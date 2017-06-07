package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARK;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.impl.CloudLayerImpl;
import fi.fmi.avi.data.impl.NumericMeasureImpl;
import fi.fmi.avi.data.impl.WeatherImpl;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer;
import fi.fmi.avi.parser.impl.lexer.token.Weather;

/**
 * Common parent class for AviMessageParser implementations.
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public abstract class AbstractAviMessageParser {

    /**
     * Finds the next {@link Lexeme} identified as <code>needle</code> in the sequence of Lexemes starting from <code>from</code>.
     *
     * @param needle
     *         the identity of the Lexeme to find
     * @param from
     *         the starting point
     *
     * @return the found Lexeme, or null if match was not found by the last Lexeme
     */
    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from) {
        return findNext(needle, from, null);
    }

    /**
     * Finds the next {@link Lexeme} in the sequence of Lexemes starting from <code>from</code> before encountering any Lexemes
     * identified as any of the values of <code>stopAt</code>.
     *
     * @param from the starting point
     * @param stopAt search boundary identities
     * @return the found Lexeme, or null if match was not found by the last Lexeme
     */
    protected static Lexeme findNext(final Lexeme from, final Lexeme.Identity[] stopAt) {
        return findNext(null, from, stopAt);
    }

    /**
     * Finds the next {@link Lexeme} identified as <code>needle</code> in the sequence of Lexemes starting
     * from <code>from</code> before encountering any Lexemes
     * identified as any of the values of <code>stopAt</code>.
     *
     * @param needle the identity of the Lexeme to find
     * @param from the starting point
     * @param stopAt search boundary identities
     * @return the found Lexeme, or null if match was not found by the last Lexeme
     */
    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt) {
        return findNext(needle, from, stopAt, null, null);
    }

    /**
     * Finds the next {@link Lexeme} identified as <code>needle</code> in the sequence of Lexemes starting
     * from <code>from</code> before encountering any Lexemes
     * identified as any of the values of <code>stopAt</code>.
     *
     * If the <code>found</code> is not null, it's {@link LexemeParsingConsumer#consume(Lexeme)} is called with the
     * possible match. If not match is found, this method is not called.
     *
     * As {@link LexemeParsingConsumer} is a functional interface, it can be implemented as a lambda expression:
     * <pre>
     *     findNext(CORRECTION, lexed.getFirstLexeme(), stopAt, (match) -> taf.setStatus(AviationCodeListUser.TAFStatus.CORRECTION));
     * </pre>
     * or, if the expression is not easily inlined:
     * <pre>
     *     findNext(AMENDMENT, lexed.getFirstLexeme(), stopAt, (match) -> {
     *       TAF.TAFStatus status = taf.getStatus();
     *         if (status != null) {
     *           retval.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR,
     *             "TAF cannot be both " + TAF.TAFStatus.AMENDMENT + " and " + status + " at " + "the same time"));
     *         } else {
     *           taf.setStatus(AviationCodeListUser.TAFStatus.AMENDMENT);
     *         }
     *     });
     * </pre>
     *
     * @param needle the identity of the Lexeme to find
     * @param from the starting point
     * @param stopAt search boundary identities
     * @param found the function to execute with the match
     * @return the found Lexeme, or null if match was not found by the last Lexeme
     */
    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found) {
        return findNext(needle, from, stopAt, found, null);
    }

    /**
     * Finds the next {@link Lexeme} identified as <code>needle</code> in the sequence of Lexemes starting
     * from <code>from</code> before encountering any Lexemes
     * identified as any of the values of <code>stopAt</code>.
     *
     * If the <code>found</code> is not null, it's {@link LexemeParsingConsumer#consume(Lexeme)} is called with the
     * possible match. If not match is found and <code>notFound</code> is not null, the function <code>notFound</code> is
     * called instead of <code>found</code>.
     *
     * @param needle the identity of the Lexeme to find
     * @param from the starting point
     * @param stopAt search boundary identities
     * @param found the function to execute with the match
     * @param notFound the function to execute if not match was found
     * @return the found Lexeme, or null if match was not found by the last Lexeme
     */
    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found, final LexemeParsingNotifyer notFound) {
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

    /**
     * Convenience method for verifying that the {@link LexemeSequence} given only contains maximum of one
     * any of the {@link Lexeme}s identified as one of <code>ids</code>.
     *
     * @param lexed sequence to check
     * @param ids the identities to verify
     * @return list the ParsingIssues to report for found extra Lexemes
     */
    protected static List<ParsingIssue> checkZeroOrOne(LexemeSequence lexed, Lexeme.Identity[] ids) {
        List<ParsingIssue> retval = new ArrayList<>();
        boolean[] oneFound = new boolean[ids.length];
        List<Lexeme> recognizedLexemes = lexed.getLexemes().stream().filter((lexeme) -> Lexeme.Status.UNRECOGNIZED != lexeme.getStatus()).collect(Collectors.toList());
        for (Lexeme l : recognizedLexemes) {
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

    protected static List<ParsingIssue> updateIssueTime(final AviationWeatherMessage msg, final LexemeSequence lexed, final Lexeme.Identity[] before, final ConversionHints hints) {
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

    protected static List<ParsingIssue> appendWeatherCodes(final Lexeme source, List<fi.fmi.avi.data.Weather> target, Lexeme.Identity[] before, final ConversionHints hints) {
        Lexeme l = source;
        List<ParsingIssue> issues = new ArrayList<>();
        while (l != null) {
            String code = l.getParsedValue(Lexeme.ParsedValueName.VALUE, String.class);
            if (code != null) {
                fi.fmi.avi.data.Weather weather = new WeatherImpl();
                weather.setCode(code);
                weather.setDescription(Weather.WEATHER_CODES.get(code));
                target.add(weather);
            }
            l = findNext(WEATHER, l, before);
        }
        return issues;
    }

    protected static fi.fmi.avi.data.CloudLayer getCloudLayer(final Lexeme match) {
        fi.fmi.avi.data.CloudLayer retval = null;
        CloudLayer.CloudCover cover = match.getParsedValue(Lexeme.ParsedValueName.COVER, CloudLayer.CloudCover.class);
        CloudLayer.CloudType type = match.getParsedValue(Lexeme.ParsedValueName.TYPE, CloudLayer.CloudType.class);
        Object value = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Object.class);
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

    protected static <T extends AviationWeatherMessage> void updateRemarks(final ParsingResult<T> result, final LexemeSequence lexed, final ConversionHints hints) {
        final T msg = result.getParsedMessage();
        findNext(Identity.REMARKS_START, lexed.getFirstLexeme(), null, (match) -> {
        	List<String> remarks = new ArrayList<>();
        	match = findNext(REMARK, match);
        	while (match != null) {
        		remarks.add(match.getTACToken());
        		match = findNext(REMARK, match);
        	}
        	if (!remarks.isEmpty()) {
        		msg.setRemarks(remarks);
            }
        });
    }

    protected static boolean endsInEndToken(final LexemeSequence lexed, final ConversionHints hints) {
        if (END_TOKEN == lexed.getLastLexeme().getIdentityIfAcceptable()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Lambda function interface to use with {@link #findNext(Identity, Lexeme, Identity[], LexemeParsingConsumer)}
     * or {@link #findNext(Identity, Lexeme, Identity[], LexemeParsingConsumer, LexemeParsingNotifyer)}.
     *
     */
    @FunctionalInterface
    interface LexemeParsingConsumer {
        void consume(final Lexeme lexeme);
    }

    /**
     * Lambda function interface to use with
     * {@link #findNext(Identity, Lexeme, Identity[], LexemeParsingConsumer, LexemeParsingNotifyer)}.
     *
     */
    @FunctionalInterface
    interface LexemeParsingNotifyer {
        void ping();
    }
}
