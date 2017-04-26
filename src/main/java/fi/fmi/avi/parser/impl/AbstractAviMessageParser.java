package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.impl.CloudLayerImpl;
import fi.fmi.avi.data.impl.NumericMeasureImpl;
import fi.fmi.avi.data.impl.WeatherImpl;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer;
import fi.fmi.avi.parser.impl.lexer.token.Weather;

/**
 * Created by rinne on 13/04/17.
 */
public abstract class AbstractAviMessageParser {

	protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from) {
       return findNext(needle, from, null);
    }

    protected static Lexeme findNext(final Lexeme from, final Lexeme.Identity[] stopAt) {
        return findNext(null, from, stopAt);
    }
    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt) {
        return findNext(needle, from, stopAt, null, null);
    }

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found) {
        return findNext(needle, from, stopAt, found, null);
    }

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found,
            final LexemeParsingNotifyer notFound) {
        Lexeme retval = null;
        Lexeme current = from.getNext();
        if (current != null) {
            boolean stop = false;
            Lexeme.Identity currentId;
            while (!stop) {
                currentId = current.getIdentityIfAcceptable();
                if (stopAt != null) {
                    for (Lexeme.Identity i : stopAt) {
                        if (i == currentId) {
                            stop = true;
                            break;
                        }
                    }
                }
                if (!stop) {
                    if (needle == null || currentId == needle) {
                        retval = current;
                    }
                    stop = !current.hasNext() || retval != null;
                }
                current = current.getNext();
            }
        }
        if (retval != null) {
            if (found != null) {
                found.consume(retval);
            }
        } else {
            if (notFound != null) {
                notFound.ping();
            }
        }
        return retval;
    }

    protected static List<ParsingIssue> checkZeroOrOne(LexemeSequence lexed, Lexeme.Identity[] ids) {
        List<ParsingIssue> retval = new ArrayList<>();
        boolean[] oneFound = new boolean[ids.length];
        Iterator<Lexeme> it = lexed.getRecognizedLexemes();
        while (it.hasNext()) {
            Lexeme l = it.next();
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] == l.getIdentity()) {
                    if (!oneFound[i]) {
                        oneFound[i] = true;
                    } else {
                        retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "More than one of " + l.getIdentity() + " in " + lexed.getTAC()));
                    }
                }
            }
        }
        return retval;
    }

    protected static List<ParsingIssue> updateIssueTime(final AviationWeatherMessage msg, final LexemeSequence lexed, final Lexeme.Identity[] before,
            final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        findNext(ISSUE_TIME, lexed.getFirstLexeme(), before, (match) -> {
            Integer day = match.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
            Integer minute = match.getParsedValue(Lexeme.ParsedValueName.MINUTE1, Integer.class);
            Integer hour = match.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
            if (day != null && minute != null && hour != null) {
                msg.setIssueDayOfMonth(day);
                msg.setIssueHour(hour);
                msg.setIssueMinute(minute);
                msg.setIssueTimeZone("UTC");
            }
        }, () -> {
            retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing at least some of the issue time components in " + lexed.getTAC()));
        });
        return retval;
    }

    protected static List<ParsingIssue> appendWeatherCodes(final Lexeme source, List<fi.fmi.avi.data.Weather> target, Lexeme.Identity[] before,
            final ParsingHints hints) {
        Lexeme l = source;
        List<ParsingIssue> issues = new ArrayList<>();
        while (l != null) {
            fi.fmi.avi.data.Weather weather = getWeather(l, issues);
            if (weather != null) {
                target.add(weather);
            }
            l = findNext(WEATHER, l, before);
        }
        return issues;
    }

    protected static fi.fmi.avi.data.Weather getWeather(final Lexeme match, final List<ParsingIssue> issues) {
        fi.fmi.avi.data.Weather retval = null;
        List<Weather.WeatherCodePart> codeParts = match.getParsedValue(Lexeme.ParsedValueName.VALUE, List.class);
        if (codeParts != null) {
            retval = new WeatherImpl();
            for (Weather.WeatherCodePart code : codeParts) {
                switch (code) {
                    case HIGH_INTENSITY:
                        retval.setIntensity(AviationCodeListUser.WeatherCodeIntensity.HIGH);
                        break;
                    case IN_VICINITY:
                        retval.setInVicinity(true);
                        break;
                    case LOW_INTENSITY:
                        retval.setIntensity(AviationCodeListUser.WeatherCodeIntensity.LOW);
                        break;
                    default: {
                        AviationCodeListUser.WeatherCodeKind kind = AviationCodeListUser.WeatherCodeKind.forCode(code.getCode());
                        if (kind != null) {
                            retval.setKind(kind);
                        } else {
                            issues.add(
                                    new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Unknown weather code " + code.getCode() + " in " + match.getTACToken()));
                        }
                        break;
                    }
                }
            }
        }
        return retval;
    }

    protected static fi.fmi.avi.data.CloudLayer getCloudLayer(final Lexeme match) {
        fi.fmi.avi.data.CloudLayer retval = null;
        CloudLayer.CloudCover cover = match.getParsedValue(Lexeme.ParsedValueName.COVER, CloudLayer.CloudCover.class);
        CloudLayer.CloudType type = match.getParsedValue(Lexeme.ParsedValueName.TYPE, CloudLayer.CloudType.class);
        Object value = match.getParsedValue(Lexeme.ParsedValueName.VALUE);
        String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);

        if (value instanceof Integer && CloudLayer.CloudCover.SKY_OBSCURED != cover) {
            retval = new CloudLayerImpl();
            switch (cover) {
                case FEW:
                    retval.setAmount(AviationCodeListUser.CloudAmount.FEW);
                    break;
                case SCATTERED:
                    retval.setAmount(AviationCodeListUser.CloudAmount.SCT);
                    break;
                case BROKEN:
                    retval.setAmount(AviationCodeListUser.CloudAmount.BKN);
                    break;
                case OVERCAST:
                    retval.setAmount(AviationCodeListUser.CloudAmount.OVC);
                    break;
                default:
                    //NOOP
                    break;
            }
            if (CloudLayer.CloudType.TOWERING_CUMULUS == type) {
                retval.setCloudType(fi.fmi.avi.data.AviationCodeListUser.CloudType.TCU);
            } else if (CloudLayer.CloudType.CUMULONIMBUS == type) {
                retval.setCloudType(fi.fmi.avi.data.AviationCodeListUser.CloudType.CB);
            }
            Integer height = (Integer) value;
            if ("hft".equals(unit)) {
                retval.setBase(new NumericMeasureImpl(height * 100, "ft"));
            } else {
                retval.setBase(new NumericMeasureImpl(height, unit));
            }
        }
        return retval;
    }

    @FunctionalInterface
    interface LexemeParsingConsumer {
        void consume(final Lexeme lexeme);
    }

    @FunctionalInterface
    interface LexemeParsingNotifyer {
        void ping();
    }
}
