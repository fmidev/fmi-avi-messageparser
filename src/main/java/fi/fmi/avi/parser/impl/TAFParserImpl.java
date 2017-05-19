package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity;
import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AMENDMENT;
import static fi.fmi.avi.parser.Lexeme.Identity.CANCELLATION;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.MAX_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.MIN_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.util.ArrayList;
import java.util.List;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.impl.CloudForecastImpl;
import fi.fmi.avi.data.impl.NumericMeasureImpl;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFAirTemperatureForecast;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.data.taf.TAFForecast;
import fi.fmi.avi.data.taf.TAFSurfaceWind;
import fi.fmi.avi.data.taf.impl.TAFAirTemperatureForecastImpl;
import fi.fmi.avi.data.taf.impl.TAFBaseForecastImpl;
import fi.fmi.avi.data.taf.impl.TAFChangeForecastImpl;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.data.taf.impl.TAFSurfaceWindImpl;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer;
import fi.fmi.avi.parser.impl.lexer.token.ForecastChangeIndicator;
import fi.fmi.avi.parser.impl.lexer.token.MetricHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.SurfaceWind;

/**
 * Created by rinne on 25/04/17.
 */
public class TAFParserImpl extends AbstractAviMessageParser implements AviMessageSpecificParser<TAF> {

    private static Identity[] zeroOrOneAllowed = { AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, CORRECTION, AMENDMENT, CANCELLATION, NIL, MIN_TEMPERATURE,
            MAX_TEMPERATURE, REMARKS_START };

    @Override
    public ParsingResult<TAF> parseMessage(final LexemeSequence lexed, final ParsingHints hints) {
        ParsingResult<TAF> retval = new ParsingResultImpl<>();
        if (endsInEndToken(lexed, hints)) {
            retval.addIssue(checkZeroOrOne(lexed, zeroOrOneAllowed));
            TAF taf = new TAFImpl();

            retval.setParsedMessage(taf);
            Identity[] stopAt = { AERODROME_DESIGNATOR, ISSUE_TIME, NIL, VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CAVOK,
                    MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };

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

            stopAt = new Identity[] { ISSUE_TIME, NIL, VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CAVOK, MIN_TEMPERATURE,
                    MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };

            findNext(AERODROME_DESIGNATOR, lexed.getFirstLexeme(), stopAt,
                    (match) -> taf.setAerodromeDesignator(match.getParsedValue(Lexeme.ParsedValueName.VALUE, String.class)), () -> {
                        retval.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Aerodrome designator not given in " + lexed.getTAC()));
                    });

            retval.addIssue(updateTAFIssueTime(taf, lexed, hints));
            stopAt = new Identity[] { VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE,
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

            if (AviationCodeListUser.TAFStatus.MISSING == taf.getStatus()) {
                return retval;
            }

            retval.addIssue(updateTAFValidTime(taf, lexed, hints));

            stopAt = new Identity[] { SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR,
                    REMARKS_START };
            findNext(CANCELLATION, lexed.getFirstLexeme(), stopAt, (match) -> {
                taf.setStatus(AviationCodeListUser.TAFStatus.CANCELLATION);
                if (match.getNext() != null) {
                    Identity nextTokenId = match.getNext().getIdentityIfAcceptable();
                    if (END_TOKEN != nextTokenId && REMARKS_START != nextTokenId) {
                        retval.addIssue(new ParsingIssue(ParsingIssue.Type.LOGICAL_ERROR,
                                "Cancelled TAF message contains extra tokens after CNL: " + lexed.toString()));
                    }
                }
            });
            
            updateRemarks(retval, lexed, hints);

            if (AviationCodeListUser.TAFStatus.CANCELLATION == taf.getStatus()) {
                return retval;
            }

            retval.addIssue(updateBaseForecast(taf, lexed, hints));
            retval.addIssue(updateChangeForecasts(taf, lexed, hints));

        } else {
            retval.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Message does not end in end token"));
        }
        return retval;
    }

    private List<ParsingIssue> updateTAFIssueTime(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        Identity[] before = new Identity[] { NIL, VALID_TIME, CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CAVOK, MIN_TEMPERATURE,
                MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateIssueTime(fct, lexed, before, hints));
        return retval;
    }

    private List<ParsingIssue> updateTAFValidTime(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        Identity[] before = new Identity[] { CANCELLATION, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CAVOK, MIN_TEMPERATURE, MAX_TEMPERATURE,
                FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(VALID_TIME, lexed.getFirstLexeme(), before, (match) -> {
            Integer startDay = match.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
            Integer endDay = match.getParsedValue(Lexeme.ParsedValueName.DAY2, Integer.class);
            Integer startHour = match.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
            Integer endHour = match.getParsedValue(Lexeme.ParsedValueName.HOUR2, Integer.class);
            if (startDay != null) {
                fct.setValidityStartDayOfMonth(startDay);
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing start day of validity" + match.getTACToken()));
            }

            if (endDay != null) {
                fct.setValidityEndDayOfMonth(endDay);
            } else {
                fct.setValidityEndDayOfMonth(startDay);
            }

            if (startHour != null) {
                fct.setValidityStartHour(startHour);
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing start hour of validity: " + match.getTACToken()));
            }

            if (endHour != null) {
                fct.setValidityEndHour(endHour);
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing end hour of validity: " + match.getTACToken()));
            }
        }, () -> {
            retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing validity"));
        });
        return retval;
    }

    private List<ParsingIssue> updateBaseForecast(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        TAFBaseForecast baseFct = new TAFBaseForecastImpl();

        Identity[] before = { CAVOK, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateForecastSurfaceWind(baseFct, lexed.getFirstLexeme(), before, hints));

        before = new Identity[] { HORIZONTAL_VISIBILITY, WEATHER, CLOUD, MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CAVOK, lexed.getFirstLexeme(), before, (match) -> baseFct.setCeilingAndVisibilityOk(true));

        before = new Identity[] { WEATHER, CLOUD, MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateVisibility(baseFct, lexed.getFirstLexeme(), before, hints));

        before = new Identity[] { CLOUD, MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateWeather(baseFct, lexed.getFirstLexeme(), before, hints));

        findNext(NO_SIGNIFICANT_CLOUD, lexed.getFirstLexeme(), before, (match) -> {
            CloudForecast cfct = baseFct.getCloud();
            if (cfct != null && (cfct.getVerticalVisibility() != null || (cfct.getLayers() != null && cfct.getLayers().size() > 0))) {
                retval.add(new ParsingIssue(ParsingIssue.Type.LOGICAL_ERROR, "Cannot have both NSC and clouds in the same change forecast"));
            } else {
                baseFct.setNoSignificantCloud(true);
            }
        });

        before = new Identity[] { MIN_TEMPERATURE, MAX_TEMPERATURE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateClouds(baseFct, lexed.getFirstLexeme(), before, hints));

        retval.addAll(updateTemperatures(baseFct, lexed.getFirstLexeme(), hints));

        fct.setBaseForecast(baseFct);
        return retval;
    }

    private List<ParsingIssue> updateTemperatures(final TAFBaseForecast baseFct, final Lexeme from, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        List<TAFAirTemperatureForecast> temps = new ArrayList<>();
        TAFAirTemperatureForecast airTemperatureForecast;
        Identity[] stopAt = { FORECAST_CHANGE_INDICATOR, REMARKS_START };
        Lexeme maxTempToken = findNext(MAX_TEMPERATURE, from, stopAt);

        if (maxTempToken != null) {
        	Lexeme minBeforeFirstMax = findNext(MIN_TEMPERATURE, from, new Identity[] { maxTempToken.getIdentity() });
        	if (minBeforeFirstMax != null) {
        		retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR,
        				"Minimum temperature given before maximum temperature: " + minBeforeFirstMax.getTACToken()));
        	}
        }
        
        Lexeme minTempToken;
        while (maxTempToken != null) {
            minTempToken = findNext(MIN_TEMPERATURE, maxTempToken, stopAt);
            if (minTempToken != null) {
                airTemperatureForecast = new TAFAirTemperatureForecastImpl();
                Integer day = minTempToken.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
                Integer hour = minTempToken.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
                Integer value = minTempToken.getParsedValue(Lexeme.ParsedValueName.VALUE, Integer.class);

                if (day != null) {
                    airTemperatureForecast.setMinTemperatureDayOfMonth(day);
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA,
                            "Missing day of month for min forecast temperature: " + minTempToken.getTACToken()));
                }

                if (hour != null) {
                    airTemperatureForecast.setMinTemperatureHour(hour);
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA,
                            "Missing hour of day for min forecast temperature: " + minTempToken.getTACToken()));
                }

                if (value != null) {
                    airTemperatureForecast.setMinTemperature(new NumericMeasureImpl(value, "degC"));
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing value for min forecast temperature: " + minTempToken.getTACToken()));
                }

                day = maxTempToken.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
                hour = maxTempToken.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
                value = maxTempToken.getParsedValue(Lexeme.ParsedValueName.VALUE, Integer.class);

                if (day != null) {
                    airTemperatureForecast.setMaxTemperatureDayOfMonth(day);
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA,
                            "Missing day of month for max forecast temperature: " + maxTempToken.getTACToken()));
                }

                if (hour != null) {
                    airTemperatureForecast.setMaxTemperatureHour(hour);
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA,
                            "Missing hour of day for max forecast temperature: " + maxTempToken.getTACToken()));
                }

                if (value != null) {
                    airTemperatureForecast.setMaxTemperature(new NumericMeasureImpl(value, "degC"));
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing value for max forecast temperature: " + maxTempToken.getTACToken()));
                }
                temps.add(airTemperatureForecast);
                maxTempToken = findNext(MAX_TEMPERATURE, minTempToken, stopAt);
            } else {
            	maxTempToken = null;
            }
        }
        if (!temps.isEmpty()) {
            baseFct.setTemperatures(temps);
        }
        return retval;
    }

    private List<ParsingIssue> updateChangeForecasts(final TAF fct, final LexemeSequence lexed, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        Identity[] stopAt = { REMARKS_START };
        findNext(FORECAST_CHANGE_INDICATOR, lexed.getFirstLexeme(), stopAt, (match) -> {
            List<TAFChangeForecast> changeForecasts = new ArrayList<>();
            while (match != null) {

                //PROB30 [TEMPO] or PROB40 [TEMPO] or BECMG or TEMPO or FM
                ForecastChangeIndicator.ForecastChangeIndicatorType type = match.getParsedValue(Lexeme.ParsedValueName.TYPE,
                        ForecastChangeIndicator.ForecastChangeIndicatorType.class);
                if (match.hasNext()) {
                    Lexeme next = match.getNext();
                    if (REMARKS_START != next.getIdentityIfAcceptable() && END_TOKEN != next.getIdentityIfAcceptable()) {
                        TAFChangeForecast changeFct = new TAFChangeForecastImpl();
                        switch (type) {
                            case TEMPORARY_FLUCTUATIONS:
                                changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.TEMPORARY_FLUCTUATIONS);
                                updateChangeForecastContents(changeFct, type, match, hints);
                                break;
                            case BECOMING:
                                changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.BECOMING);
                                updateChangeForecastContents(changeFct, type, match, hints);
                                break;
                            case FROM:
                                changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.FROM);
                                Integer day = match.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
                                Integer hour = match.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
                                Integer minute = match.getParsedValue(Lexeme.ParsedValueName.MINUTE1, Integer.class);
                                if (day != null) {
                                    changeFct.setValidityStartDayOfMonth(day);
                                }
                                if (hour != null && minute != null) {
                                    changeFct.setValidityStartHour(hour);
                                    changeFct.setValidityStartMinute(minute);
                                } else {
                                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA,
                                            "Missing validity start hour or minute in " + match.getTACToken()));
                                }
                                updateChangeForecastContents(changeFct, type, match, hints);
                                break;
                            case WITH_40_PCT_PROBABILITY:
                            case WITH_30_PCT_PROBABILITY: {
                                if (FORECAST_CHANGE_INDICATOR == next.getIdentityIfAcceptable()) {
                                    if (ForecastChangeIndicator.ForecastChangeIndicatorType.TEMPORARY_FLUCTUATIONS == next.getParsedValue(
                                            Lexeme.ParsedValueName.TYPE, ForecastChangeIndicator.ForecastChangeIndicatorType.class)) {
                                        if (ForecastChangeIndicator.ForecastChangeIndicatorType.WITH_30_PCT_PROBABILITY == type) {
                                            changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.PROBABILITY_30_TEMPORARY_FLUCTUATIONS);
                                        } else {
                                            changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.PROBABILITY_40_TEMPORARY_FLUCTUATIONS);
                                        }
                                        updateChangeForecastContents(changeFct, type, next, hints);
                                        match = next;
                                    } else {
                                        retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, type + " cannot be followed by " + next));
                                    }
                                } else {
                                    if (ForecastChangeIndicator.ForecastChangeIndicatorType.WITH_30_PCT_PROBABILITY == type) {
                                        changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.PROBABILITY_30);
                                    } else {
                                        changeFct.setChangeIndicator(AviationCodeListUser.TAFChangeIndicator.PROBABILITY_40);
                                    }
                                    updateChangeForecastContents(changeFct, type, match, hints);
                                }
                                break;
                            }
                            case AT:
                            case UNTIL:
                                retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Change group " + type + " is not allowed in TAF"));
                                break;
                            default:
                                retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Unknonw change group " + type));
                                break;
                        }
                        changeForecasts.add(changeFct);
                    } else {
                        retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing change group content"));
                    }
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing change group content"));
                }
                match = findNext(FORECAST_CHANGE_INDICATOR, match, stopAt);
            }
            if (!changeForecasts.isEmpty()) {
                fct.setChangeForecasts(changeForecasts);
            }
        });
        return retval;
    }

    private List<ParsingIssue> updateChangeForecastContents(final TAFChangeForecast fct, final ForecastChangeIndicator.ForecastChangeIndicatorType type,
            final Lexeme from, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        Identity[] before = { CAVOK, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        if (ForecastChangeIndicator.ForecastChangeIndicatorType.FROM != type) {
            Lexeme timeGroup = findNext(CHANGE_FORECAST_TIME_GROUP, from, before);
            if (timeGroup != null) {
                Integer startDay = timeGroup.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
                Integer endDay = timeGroup.getParsedValue(Lexeme.ParsedValueName.DAY2, Integer.class);
                Integer startHour = timeGroup.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
                Integer endHour = timeGroup.getParsedValue(Lexeme.ParsedValueName.HOUR2, Integer.class);
                if (endDay != null) {
                    fct.setValidityEndDayOfMonth(endDay);
                }
                if (startDay != null && startHour != null && endHour != null) {
                    fct.setValidityStartDayOfMonth(startDay);
                    fct.setValidityStartHour(startHour);
                    fct.setValidityEndHour(endHour);
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA,
                            "Missing validity day, hour or minute for change group in " + timeGroup.getTACToken()));
                }
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing validity time for change group after " + from.getTACToken()));
            }
        }

        before = new Identity[] { CAVOK, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateForecastSurfaceWind(fct, from, before, hints));

        before = new Identity[] { HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CAVOK, from, before, (match) -> fct.setCeilingAndVisibilityOk(true));

        before = new Identity[] { WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateVisibility(fct, from, before, hints));

        before = new Identity[] { CLOUD, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateWeather(fct, from, before, hints));

        findNext(NO_SIGNIFICANT_WEATHER, from, before, (match) -> {
            if (fct.getForecastWeather() != null && !fct.getForecastWeather().isEmpty()) {
                retval.add(new ParsingIssue(ParsingIssue.Type.LOGICAL_ERROR, "Cannot have both NSW and weather in the same change forecast"));
            } else {
                fct.setNoSignificantWeather(true);
            }
        });

        before = new Identity[] { FORECAST_CHANGE_INDICATOR, REMARKS_START };
        retval.addAll(updateClouds(fct, from, before, hints));

        findNext(NO_SIGNIFICANT_CLOUD, from, before, (match) -> {
            CloudForecast cfct = fct.getCloud();
            if (cfct != null && (cfct.getVerticalVisibility() != null || (cfct.getLayers() != null && cfct.getLayers().size() > 0))) {
                retval.add(new ParsingIssue(ParsingIssue.Type.LOGICAL_ERROR, "Cannot have both NSC and clouds in the same change forecast"));
            } else {
                fct.setNoSignificantCloud(true);
            }
        });

        return retval;
    }

    private List<ParsingIssue> updateForecastSurfaceWind(final TAFForecast fct, final Lexeme from, final Identity[] before, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        findNext(SURFACE_WIND, from, before, (match) -> {
            TAFSurfaceWind wind = new TAFSurfaceWindImpl();
            Object direction = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION, Object.class);
            Integer meanSpeed = match.getParsedValue(Lexeme.ParsedValueName.MEAN_VALUE, Integer.class);
            Integer gustSpeed = match.getParsedValue(Lexeme.ParsedValueName.MAX_VALUE, Integer.class);
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);

            if (direction == SurfaceWind.WindDirection.VARIABLE) {
                wind.setVariableDirection(true);
            } else if (direction instanceof Integer) {
                wind.setMeanWindDirection(new NumericMeasureImpl((Integer) direction, "deg"));
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Surface wind direction is missing: " + match.getTACToken()));
            }

            if (meanSpeed != null) {
                wind.setMeanWindSpeed(new NumericMeasureImpl(meanSpeed, unit));
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Surface wind mean speed is missing: " + match.getTACToken()));
            }

            if (gustSpeed != null) {
                wind.setWindGust(new NumericMeasureImpl(gustSpeed, unit));
            }
            fct.setSurfaceWind(wind);
        }, () -> {
            if (fct instanceof TAFBaseForecast) {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Surface wind is missing from TAF base forecast"));
            }
        });
        return retval;
    }

    private List<ParsingIssue> updateVisibility(final TAFForecast fct, final Lexeme from, final Identity[] before, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        findNext(HORIZONTAL_VISIBILITY, from, before, (match) -> {
            Double distance = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Double.class);
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
            RecognizingAviMessageTokenLexer.RelationalOperator distanceOperator = match.getParsedValue(Lexeme.ParsedValueName.RELATIONAL_OPERATOR,
                    RecognizingAviMessageTokenLexer.RelationalOperator.class);
            MetricHorizontalVisibility.DirectionValue direction = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION,
                    MetricHorizontalVisibility.DirectionValue.class);
            if (direction != null) {
                retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Directional horizontal visibility not allowed in TAF: " + match.getTACToken()));
            }
            if (distance != null && unit != null) {
                fct.setPrevailingVisibility(new NumericMeasureImpl(distance, unit));
            } else {
                retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing visibility value or unit: " + match.getTACToken()));
            }
            if (distanceOperator != null) {
                if (RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN == distanceOperator) {
                    fct.setPrevailingVisibilityOperator(AviationCodeListUser.RelationalOperator.BELOW);
                } else {
                    fct.setPrevailingVisibilityOperator(AviationCodeListUser.RelationalOperator.ABOVE);
                }
            }
        }, () -> {
            if (fct instanceof TAFBaseForecast) {
                if (!fct.isCeilingAndVisibilityOk()) {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Visibility or CAVOK is missing from TAF base forecast"));
                }
            }
        });
        return retval;
    }

    private List<ParsingIssue> updateWeather(final TAFForecast fct, final Lexeme from, final Identity[] before, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        findNext(WEATHER, from, before, (match) -> {
            if (match != null) {
                List<fi.fmi.avi.data.Weather> weather = new ArrayList<>();
                retval.addAll(appendWeatherCodes(match, weather, before, hints));
                if (!weather.isEmpty()) {
                    fct.setForecastWeather(weather);
                }
            }
        });
        return retval;
    }

    private List<ParsingIssue> updateClouds(final TAFForecast fct, final Lexeme from, final Identity[] before, final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        findNext(CLOUD, from, before, (match) -> {
            CloudForecast cloud = new CloudForecastImpl();
            List<fi.fmi.avi.data.CloudLayer> layers = new ArrayList<>();
            while (match != null) {
                CloudLayer.CloudCover cover = match.getParsedValue(Lexeme.ParsedValueName.COVER, CloudLayer.CloudCover.class);
                Object value = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Object.class);
                String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
                if (value instanceof Integer) {
                    if (CloudLayer.CloudCover.SKY_OBSCURED == cover) {
                        int height = ((Integer) value).intValue();
                        if ("hft".equals(unit)) {
                            height = height * 100;
                            unit = "ft";
                        }
                        cloud.setVerticalVisibility(new NumericMeasureImpl(height, unit));
                    } else {
                        fi.fmi.avi.data.CloudLayer layer = getCloudLayer(match);
                        if (layer != null) {
                            layers.add(layer);
                        } else {
                            retval.add(
                                    new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Could not parse token " + match.getTACToken() + " as cloud " + "layer"));
                        }
                    }
                } else {
                    retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Cloud layer height is not an integer in " + match.getTACToken()));
                }
                match = findNext(CLOUD, match, before);
            }
            if (!layers.isEmpty()) {
                cloud.setLayers(layers);
            }
            fct.setCloud(cloud);
        }, () -> {
            if (fct instanceof TAFBaseForecast) {
                if (!fct.isCeilingAndVisibilityOk()) {
                    retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Cloud or CAVOK is missing from TAF base forecast"));
                }
            }
        });
        return retval;
    }

}
