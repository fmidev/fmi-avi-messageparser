package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity;
import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AMENDMENT;
import static fi.fmi.avi.parser.Lexeme.Identity.CANCELLATION;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.MAX_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.MIN_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.util.ArrayList;
import java.util.List;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFForecast;
import fi.fmi.avi.data.taf.impl.TAFBaseForecastImpl;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult;

/**
 * Created by rinne on 25/04/17.
 */
public class TAFParserImpl extends AbstractAviMessageParser implements AviMessageSpecificParser<TAF> {

    private static Identity[] zeroOrOneAllowed = { AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, CORRECTION, AMENDMENT, CANCELLATION, NIL };

    @Override
    public ParsingResult<TAF> parseMessage(final LexemeSequence lexed, final ParsingHints hints) {
        ParsingResult<TAF> retval = new ParsingResultImpl<>();
        retval.addIssue(checkZeroOrOne(lexed, zeroOrOneAllowed));
        TAF taf = new TAFImpl();

        Identity[] stopAt = { AERODROME_DESIGNATOR, ISSUE_TIME, NIL, VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CAVOK,
                MIN_TEMPERATURE, MAX_TEMPERATURE, WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };

        findNext(CORRECTION, lexed.getFirstLexeme(), stopAt, (match) -> taf.setStatus(AviationCodeListUser.TAFStatus.CORRECTION));

        findNext(AMENDMENT, lexed.getFirstLexeme(), stopAt, (match) -> {
            TAF.TAFStatus status = taf.getStatus();
            if (status != null) {
                retval.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR,
                        "TAF cannot be both " + TAF.TAFStatus.AMENDMENT + " and " + status + " at " + "the same time"));
            } else {
                taf.setStatus(AviationCodeListUser.TAFStatus.AMENDMENT);
            }
        });

        if (taf.getStatus() == null) {
            taf.setStatus(AviationCodeListUser.TAFStatus.NORMAL);
        }

        stopAt = new Identity[] { ISSUE_TIME, NIL, VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CAVOK, MIN_TEMPERATURE,
                MAX_TEMPERATURE, WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };

        findNext(AERODROME_DESIGNATOR, lexed.getFirstLexeme(), stopAt,
                (match) -> taf.setAerodromeDesignator(match.getParsedValue(Lexeme.ParsedValueName.VALUE, String.class)), () -> {
                    retval.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Aerodrome designator not given in " + lexed.getTAC()));
                });

        retval.addIssue(updateTAFIssueTime(taf, lexed, hints));
        stopAt = new Identity[] { VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE, WEATHER,
                FORECAST_CHANGE_INDICATOR, REMARKS_START };

        findNext(NIL, lexed.getFirstLexeme(), stopAt, (match) -> {
            taf.setStatus(AviationCodeListUser.TAFStatus.MISSING);
            if (match.getNext() != null) {
                Identity nextTokenId = match.getNext().getIdentityIfAcceptable();
                if (END_TOKEN != nextTokenId && REMARKS_START != nextTokenId) {
                    retval.addIssue(
                            new ParsingIssue(ParsingIssue.Type.LOGICAL_ERROR, "Missing TAF message contains extra tokens after NIL: " + lexed.toString()));
                }
            }
        });

        retval.addIssue(updateTAFValidTime(taf, lexed, hints));

        stopAt = new Identity[] { SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE, WEATHER, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
        findNext(CANCELLATION, lexed.getFirstLexeme(), stopAt, (match) -> {
            taf.setStatus(AviationCodeListUser.TAFStatus.CANCELLATION);
            if (match.getNext() != null) {
                Identity nextTokenId = match.getNext().getIdentityIfAcceptable();
                if (END_TOKEN != nextTokenId && REMARKS_START != nextTokenId) {
                    retval.addIssue(
                            new ParsingIssue(ParsingIssue.Type.LOGICAL_ERROR, "Cancelled TAF message contains extra tokens after CNL: " + lexed.toString()));
                }
            }
        });

        retval.addIssue(updateBaseForecast(taf, lexed, hints));
        retval.addIssue(updateChangeForecasts(taf, lexed, hints));

        retval.setParsedMessage(taf);

        return retval;
    }

    private List<ParsingIssue> updateTAFIssueTime(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        Identity[] before = new Identity[] { NIL, VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE,
                WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateIssueTime(fct, lexed, before, hints));
        return retval;
    }

    private List<ParsingIssue> updateTAFValidTime(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        Identity[] before = new Identity[] { CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE, WEATHER,
                FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(VALID_TIME, lexed.getFirstLexeme(), before, (match) -> {
            Integer startDay = match.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
            Integer endDay = match.getParsedValue(Lexeme.ParsedValueName.DAY2, Integer.class);
            Integer startHour = match.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
            Integer endHour = match.getParsedValue(Lexeme.ParsedValueName.HOUR2, Integer.class);
            if (startDay != null) {
                fct.setValidityStartDayOfMonth(startDay);
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing start day of validity"));
            }

            if (endDay != null) {
                fct.setValidityEndDayOfMonth(endDay);
            } else {
                fct.setValidityEndDayOfMonth(startDay);
            }

            if (startHour != null) {
                fct.setValidityStartHour(startHour);
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing start hour of validity"));
            }

            if (endHour != null) {
                fct.setValidityEndHour(endHour);
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing end hour of validity"));
            }
        }, () -> {
            retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing validity"));
        });
        return retval;
    }

    private List<ParsingIssue> updateBaseForecast(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        TAFBaseForecast baseFct = new TAFBaseForecastImpl();
        //TODO: temperatures
        retval.addAll(updateForecastSurfaceWind(baseFct, lexed, hints));
        retval.addAll(updateVisibility(baseFct, lexed, hints));
        retval.addAll(updateWeather(baseFct, lexed, hints));
        retval.addAll(updateClouds(baseFct, lexed, hints));

        fct.setBaseForecast(baseFct);
        return retval;
    }

    private List<ParsingIssue> updateChangeForecasts(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();

        return retval;
    }

    private List<ParsingIssue> updateForecastSurfaceWind(final TAFForecast fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        return retval;
    }

    private List<ParsingIssue> updateVisibility(final TAFForecast fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        return retval;
    }

    private List<ParsingIssue> updateWeather(final TAFForecast fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        return retval;
    }

    private List<ParsingIssue> updateClouds(final TAFForecast fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        return retval;
    }

}
