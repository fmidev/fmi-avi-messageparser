package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.RECENT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_VISUAL_RANGE;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.VARIABLE_WIND_DIRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.impl.CloudLayerImpl;
import fi.fmi.avi.data.impl.NumericMeasureImpl;
import fi.fmi.avi.data.metar.HorizontalVisibility;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.ObservedSurfaceWind;
import fi.fmi.avi.data.metar.RunwayVisualRange;
import fi.fmi.avi.data.metar.impl.HorizontalVisibilityImpl;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.data.metar.impl.ObservedCloudsImpl;
import fi.fmi.avi.data.metar.impl.ObservedSurfaceWindImpl;
import fi.fmi.avi.data.metar.impl.RunwayVisualRangeImpl;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingException;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.token.AtmosphericPressureQNH;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer;
import fi.fmi.avi.parser.impl.lexer.token.MetricHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.SurfaceWind;
import fi.fmi.avi.parser.impl.lexer.token.Weather;

/**
 * Created by rinne on 13/04/17.
 */
public class MetarParserImpl extends AbstractAviMessageParser implements AviMessageSpecificParser<Metar> {

    private static final Logger LOG = LoggerFactory.getLogger(MetarParserImpl.class);

    public Metar parseMessage(final LexemeSequence lexed, final ParsingHints hints) throws ParsingException {
        Metar retval = new MetarImpl();

        Lexeme.Identity[] stopAt = { AERODROME_DESIGNATOR };
        findNext(CORRECTION, lexed.getFirstLexeme(), stopAt, (match) -> retval.setStatus(AviationCodeListUser.MetarStatus.CORRECTION),
                () -> retval.setStatus(AviationCodeListUser.MetarStatus.NORMAL));

        stopAt = new Lexeme.Identity[] { ISSUE_TIME };
        findNext(AERODROME_DESIGNATOR, lexed.getFirstLexeme(), stopAt,
                (match) -> retval.setAerodromeDesignator(match.getParsedValue(Lexeme.ParsedValueName.VALUE, String.class)), () -> {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Aerodrome designator not given");
                });

        updateMetarIssueTime(retval, lexed);
        updateSurfaceWind(retval, lexed);

        stopAt = new Lexeme.Identity[] { HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
        findNext(CAVOK, lexed.getFirstLexeme(), stopAt, (match) -> retval.setCeilingAndVisibilityOk(true));

        updateHorizontalVisibility(retval, lexed);
        updateRVR(retval, lexed);
        updateWeather(retval, lexed, false);
        updateClouds(retval, lexed);
        updateTemperatures(retval, lexed);
        updateQNH(retval, lexed);
        updateWeather(retval, lexed, true);
        updateWindShear(retval, lexed);
        updateSeaState(retval, lexed);
        updateRunwayStates(retval, lexed);
        updateTrends(retval, lexed);

        return retval;
    }

    private static void updateMetarIssueTime(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { SURFACE_WIND };
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
            throw new ParsingException(ParsingException.Type.MISSING_DATA, "Missing at least some of the issue time components");
        });

    }

    private static void updateSurfaceWind(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
        findNext(SURFACE_WIND, lexed.getFirstLexeme(), before, (match) -> {
            Object direction = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION, Integer.class);
            Integer meanSpeed = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION, Integer.class);
            Integer gust = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION, Integer.class);
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);

            final ObservedSurfaceWind wind = new ObservedSurfaceWindImpl();

            if (direction == SurfaceWind.WindDirection.VARIABLE) {
                wind.setVariableDirection(true);
            } else if (direction != null && direction instanceof Integer) {
                wind.setMeanWindDirection(new NumericMeasureImpl((Integer) direction, "deg"));
            } else {
                throw new ParsingException(ParsingException.Type.MISSING_DATA, "Direction missing for surface wind");
            }

            if (meanSpeed != null) {
                wind.setMeanWindSpeed(new NumericMeasureImpl(meanSpeed, unit));
            } else {
                throw new ParsingException(ParsingException.Type.MISSING_DATA, "Mean speed missing for surface wind");
            }

            if (gust != null) {
                wind.setWindGust(new NumericMeasureImpl(gust, unit));
            }

            findNext(VARIABLE_WIND_DIRECTION, match, before, (match2) -> {
                Integer maxDirection = match2.getParsedValue(Lexeme.ParsedValueName.MAX_DIRECTION, Integer.class);
                Integer minDirection = match2.getParsedValue(Lexeme.ParsedValueName.MIN_DIRECTION, Integer.class);

                if (minDirection != null) {
                    wind.setExtremeCounterClockwiseWindDirection(new NumericMeasureImpl(minDirection, "deg"));
                }
                if (maxDirection != null) {
                    wind.setExtremeClockwiseWindDirection(new NumericMeasureImpl(maxDirection, "deg"));
                }
            });
            msg.setSurfaceWind(wind);
        }, () -> {
            //TODO: cases where it's ok to be missing the surface wind
            throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Missing surface wind information");
        });
    }

    private static void updateHorizontalVisibility(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(HORIZONTAL_VISIBILITY, lexed.getFirstLexeme(), before, (match) -> {
            MetricHorizontalVisibility.DirectionValue direction = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION,
                    MetricHorizontalVisibility.DirectionValue.class);
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
            Integer value = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Integer.class);
            RecognizingAviMessageTokenLexer.RelationalOperator operator = match.getParsedValue(Lexeme.ParsedValueName.RELATIONAL_OPERATOR,
                    RecognizingAviMessageTokenLexer.RelationalOperator.class);

            HorizontalVisibility vis = new HorizontalVisibilityImpl();
            if (direction != null) {
                vis.setMinimumVisibility(new NumericMeasureImpl(value, unit));
                vis.setMinimumVisibilityDirection(new NumericMeasureImpl(direction.inDegrees(), "deg"));
            } else {
                vis.setPrevailingVisibility(new NumericMeasureImpl(value, unit));
                if (RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN == operator) {
                    vis.setPrevailingVisibilityOperator(AviationCodeListUser.RelationalOperator.BELOW);
                } else if (RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN == operator) {
                    vis.setPrevailingVisibilityOperator(AviationCodeListUser.RelationalOperator.ABOVE);
                }
            }
            msg.setVisibility(vis);
        }, () -> {
            throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Missing horizontal visibility information");
        });
    }

    private static void updateRVR(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(RUNWAY_VISUAL_RANGE, lexed.getFirstLexeme(), before, (match) -> {
            List<RunwayVisualRange> rvrs = new ArrayList<>();
            while (match != null) {
                String runway = match.getParsedValue(Lexeme.ParsedValueName.RUNWAY, String.class);
                Integer minValue = match.getParsedValue(Lexeme.ParsedValueName.MIN_VALUE, Integer.class);
                RecognizingAviMessageTokenLexer.RelationalOperator minValueOperator = match.getParsedValue(Lexeme.ParsedValueName.RELATIONAL_OPERATOR,
                        RecognizingAviMessageTokenLexer.RelationalOperator.class);
                Integer maxValue = match.getParsedValue(Lexeme.ParsedValueName.MAX_VALUE, Integer.class);
                RecognizingAviMessageTokenLexer.RelationalOperator maxValueOperator = match.getParsedValue(Lexeme.ParsedValueName.RELATIONAL_OPERATOR2,
                        RecognizingAviMessageTokenLexer.RelationalOperator.class);
                RecognizingAviMessageTokenLexer.TendencyOperator tendencyIndicator = match.getParsedValue(Lexeme.ParsedValueName.TENDENCY_OPERATOR,
                        RecognizingAviMessageTokenLexer.TendencyOperator.class);
                String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);

                if (runway == null) {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Missing runway code for RVR in " + match.getTACToken());
                }
                if (minValue == null) {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Missing visibility value for RVR in " + match.getTACToken());
                }
                RunwayVisualRange rvr = new RunwayVisualRangeImpl();
                rvr.setRunwayDirectionDesignator(runway);
                if (maxValue != null) {
                    //Varying format, more than/less than operators ignored, as IWXXM 1.0 does not have a place for them:
                    if (minValueOperator != null) {
                        LOG.warn("Ignoring relational operator for the minimum of the varying RVR " + minValueOperator);
                    }
                    if (maxValueOperator != null) {
                        LOG.warn("Ignoring relational operator for the maximum of the varying RVR " + maxValueOperator);
                    }
                    rvr.setVaryingRVRMinimum(new NumericMeasureImpl(minValue, unit));
                    rvr.setVaryingRVRMaximum(new NumericMeasureImpl(maxValue, unit));
                } else {
                    rvr.setMeanRVR(new NumericMeasureImpl(minValue, unit));
                    if (RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN == minValueOperator) {
                        rvr.setMeanRVROperator(AviationCodeListUser.RelationalOperator.BELOW);
                    } else if (RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN == minValueOperator) {
                        rvr.setMeanRVROperator(AviationCodeListUser.RelationalOperator.ABOVE);
                    }
                }
                if (RecognizingAviMessageTokenLexer.TendencyOperator.DOWNWARD == tendencyIndicator) {
                    rvr.setPastTendency(AviationCodeListUser.VisualRangeTendency.DOWNWARD);
                } else if (RecognizingAviMessageTokenLexer.TendencyOperator.UPWARD == tendencyIndicator) {
                    rvr.setPastTendency(AviationCodeListUser.VisualRangeTendency.UPWARD);
                } else if (RecognizingAviMessageTokenLexer.TendencyOperator.NO_CHANGE == tendencyIndicator) {
                    rvr.setPastTendency(AviationCodeListUser.VisualRangeTendency.NO_CHANGE);
                }
                rvrs.add(rvr);
                match = findNext(RUNWAY_VISUAL_RANGE, match, before);
            }
            msg.setRunwayVisualRanges(rvrs);
        });
    }

    private static void updateWeather(final Metar msg, final LexemeSequence lexed, final boolean isRecent) throws ParsingException {
        Lexeme.Identity[] before = { CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        final Lexeme.Identity toMatch = isRecent ? RECENT_WEATHER : WEATHER;
        findNext(toMatch, lexed.getFirstLexeme(), before, (match) -> {
            List<String> weather = new ArrayList<>();
            while (match != null) {
                @SuppressWarnings("unchecked")
                List<Weather.WeatherCodePart> codes = match.getParsedValue(Lexeme.ParsedValueName.VALUE, List.class);

                if (codes != null) {
                    for (Weather.WeatherCodePart code : codes) {
                        weather.add(code.getCode());
                    }
                }
                match = findNext(toMatch, match, before);
            }
            if (weather != null) {
                if (isRecent) {
                    msg.setRecentWeatherCodes(weather);
                } else {
                    msg.setPresentWeatherCodes(weather);
                }
            }
        });
    }

    private static void updateClouds(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CLOUD, lexed.getFirstLexeme(), before, (match) -> {
            ObservedClouds clouds = new ObservedCloudsImpl();
            List<fi.fmi.avi.data.CloudLayer> layers = new ArrayList<>();

            while (match != null) {
                CloudLayer.CloudCover cover = match.getParsedValue(Lexeme.ParsedValueName.COVER, CloudLayer.CloudCover.class);
                CloudLayer.CloudType type = match.getParsedValue(Lexeme.ParsedValueName.TYPE, CloudLayer.CloudType.class);
                Object value = match.getParsedValue(Lexeme.ParsedValueName.VALUE);
                String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);

                if (CloudLayer.SpecialValue.AMOUNT_AND_HEIGHT_UNOBSERVABLE_BY_AUTO_SYSTEM == value) {
                    clouds.setAmountAndHeightUnobservableByAutoSystem(true);
                } else if (value instanceof Integer) {
                    Integer height = (Integer) value;
                    if (CloudLayer.CloudCover.SKY_OBSCURED == cover) {
                        clouds.setVerticalVisibility(new NumericMeasureImpl(height, unit));
                    } else {
                        fi.fmi.avi.data.CloudLayer layer = new CloudLayerImpl();
                        switch (cover) {
                            case FEW:
                                layer.setAmount(AviationCodeListUser.CloudAmount.FEW);
                                break;
                            case SCATTERED:
                                layer.setAmount(AviationCodeListUser.CloudAmount.SCT);
                                break;
                            case BROKEN:
                                layer.setAmount(AviationCodeListUser.CloudAmount.BKN);
                                break;
                            case OVERCAST:
                                layer.setAmount(AviationCodeListUser.CloudAmount.OVC);
                                break;
                            case SKY_OBSCURED:
                            case NO_SIG_CLOUDS:
                            case NO_LOW_CLOUDS:
                                //NOOP
                        }
                        if (CloudLayer.CloudType.TOWERING_CUMULUS == type) {
                            layer.setCloudType(fi.fmi.avi.data.AviationCodeListUser.CloudType.TCU);
                        } else if (CloudLayer.CloudType.CUMULONIMBUS == type) {
                            layer.setCloudType(fi.fmi.avi.data.AviationCodeListUser.CloudType.CB);
                        }
                        if ("hft".equals(unit)) {
                            layer.setBase(new NumericMeasureImpl(height * 100, "ft"));
                        } else {
                            layer.setBase(new NumericMeasureImpl(height, unit));
                        }
                        layers.add(layer);
                    }
                } else {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Cloud layer height is not an integer in " + match.getTACToken());
                }

                match = findNext(CLOUD, match, before);
            }
            if (layers != null && clouds != null) {
                clouds.setLayers(layers);
            }
            msg.setClouds(clouds);
        });

    }

    private static void updateTemperatures(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(AIR_DEWPOINT_TEMPERATURE, lexed.getFirstLexeme(), before, (match) -> {
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
            Integer[] values = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Integer[].class);
            if (values[0] != null) {
                msg.setAirTemperature(new NumericMeasureImpl(values[0], unit));
            } else {
                throw new ParsingException(ParsingException.Type.MISSING_DATA, "Missing air temperature value");
            }
            if (values[1] != null) {
                msg.setDewpointTemperature(new NumericMeasureImpl(values[1], unit));
            } else {
                throw new ParsingException(ParsingException.Type.MISSING_DATA, "Missing dewpoint temperature value");
            }
        }, () -> {
            throw new ParsingException(ParsingException.Type.MISSING_DATA, "Missing air temperature and dewpoint temperature values");
        });

    }

    private static void updateQNH(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(AIR_PRESSURE_QNH, lexed.getFirstLexeme(), before, (match) -> {
            AtmosphericPressureQNH.PressureMeasurementUnit unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT,
                    AtmosphericPressureQNH.PressureMeasurementUnit.class);
            Integer value = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Integer.class);
            if (value != null) {
                String unitStr = "";
                if (unit == AtmosphericPressureQNH.PressureMeasurementUnit.HECTOPASCAL) {
                    unitStr = "hPa";
                } else if (unit == AtmosphericPressureQNH.PressureMeasurementUnit.INCHES_OF_MERCURY) {
                    unitStr = "in Hg";
                }
                msg.setAltimeterSettingQNH(new NumericMeasureImpl(value, unitStr));
            } else {
                throw new ParsingException(ParsingException.Type.MISSING_DATA, "Missing air pressure value");
            }
        }, () -> {
            throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "QNH missing");
        });
    }

    private static void updateWindShear(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(AIR_PRESSURE_QNH, lexed.getFirstLexeme(), before, (match) -> {

        });
    }

    private static void updateSeaState(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        //TODO
    }

    private static void updateRunwayStates(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        //TODO
    }

    private static void updateTrends(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        //TODO
    }

}
