package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity;
import static fi.fmi.avi.parser.Lexeme.Identity.*;

import java.util.ArrayList;
import java.util.Iterator;
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
import fi.fmi.avi.data.metar.SeaState;
import fi.fmi.avi.data.metar.WindShear;
import fi.fmi.avi.data.metar.impl.HorizontalVisibilityImpl;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.data.metar.impl.ObservedCloudsImpl;
import fi.fmi.avi.data.metar.impl.ObservedSurfaceWindImpl;
import fi.fmi.avi.data.metar.impl.RunwayVisualRangeImpl;
import fi.fmi.avi.data.metar.impl.SeaStateImpl;
import fi.fmi.avi.data.metar.impl.WindShearImpl;
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

    private static Identity[] zeroOrOneAllowed = { AERODROME_DESIGNATOR, ISSUE_TIME, CAVOK, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, WIND_SHEAR, SEA_STATE,
            REMARKS_START };

    public Metar parseMessage(final LexemeSequence lexed, final ParsingHints hints) throws ParsingException {

        boolean[] oneFound = new boolean[zeroOrOneAllowed.length];
        Iterator<Lexeme> it = lexed.getRecognizedLexemes();
        while (it.hasNext()) {
            Lexeme l = it.next();
            for (int i = 0; i < zeroOrOneAllowed.length; i++) {
                if (zeroOrOneAllowed[i] == l.getIdentity()) {
                    if (!oneFound[i]) {
                        oneFound[i] = true;
                    } else {
                        throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "More that one of " + l.getIdentity() + " in METAR");
                    }
                }
            }
        }
        Metar retval = new MetarImpl();

        Identity[] stopAt = { AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CORRECTION, lexed.getFirstLexeme(), stopAt, (match) -> retval.setStatus(AviationCodeListUser.MetarStatus.CORRECTION),
                () -> retval.setStatus(AviationCodeListUser.MetarStatus.NORMAL));

        stopAt = new Identity[] { ISSUE_TIME, SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER,
                WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(AERODROME_DESIGNATOR, lexed.getFirstLexeme(), stopAt,
                (match) -> retval.setAerodromeDesignator(match.getParsedValue(Lexeme.ParsedValueName.VALUE, String.class)), () -> {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "Aerodrome designator not given");
                });

        updateMetarIssueTime(retval, lexed);
        updateSurfaceWind(retval, lexed);

        stopAt = new Identity[] { HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR,
                SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CAVOK, lexed.getFirstLexeme(), stopAt, (match) -> retval.setCeilingAndVisibilityOk(true));

        updateHorizontalVisibility(retval, lexed);
        updateRVR(retval, lexed);
        updatePresentWeather(retval, lexed);
        updateClouds(retval, lexed);
        updateTemperatures(retval, lexed);
        updateQNH(retval, lexed);
        updateRecentWeather(retval, lexed);
        updateWindShear(retval, lexed);
        updateSeaState(retval, lexed);
        updateRunwayStates(retval, lexed);
        updateTrends(retval, lexed);
        updateRemarks(retval, lexed);

        return retval;
    }

    private static void updateMetarIssueTime(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Identity[] before = { SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
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
        Identity[] before = { CAVOK, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR,
                SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
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
        Identity[] before = { RUNWAY_VISUAL_RANGE, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE,
                FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(HORIZONTAL_VISIBILITY, lexed.getFirstLexeme(), before, (match) -> {
            MetricHorizontalVisibility.DirectionValue direction = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION,
                    MetricHorizontalVisibility.DirectionValue.class);
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
            Double value = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Double.class);
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
        Identity[] before = { CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
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

    private static void updatePresentWeather(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Identity[] before = { CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
        Lexeme match = findNext(WEATHER, lexed.getFirstLexeme(), before);
        if (match != null) {
            List<String> weather = new ArrayList<>();
            appendWeatherCodes(match, weather, before);
            if (!weather.isEmpty()) {
                msg.setPresentWeatherCodes(weather);
            }
        }
    }

    private static void appendWeatherCodes(final Lexeme source, List<String> target, Identity[] before) {
        Lexeme l = source;
        while (l != null) {
            @SuppressWarnings("unchecked")
            List<Weather.WeatherCodePart> codes = l.getParsedValue(Lexeme.ParsedValueName.VALUE, List.class);
            if (codes != null) {
                for (Weather.WeatherCodePart code : codes) {
                    target.add(code.getCode());
                }
            }
            l = findNext(WEATHER, source, before);
        }
    }

    private static void updateClouds(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Identity[] before = { AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
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
        Identity[] before = { AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
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
        Identity[] before = { RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
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

    private static void updateRecentWeather(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Identity[] before = { WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        Lexeme match = findNext(RECENT_WEATHER, lexed.getFirstLexeme(), before);
        if (match != null) {
            List<String> weather = new ArrayList<>();
            appendWeatherCodes(match, weather, before);
            if (!weather.isEmpty()) {
                msg.setRecentWeatherCodes(weather);
            }
        }
    }

    private static void updateWindShear(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Identity[] before = { SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(WIND_SHEAR, lexed.getFirstLexeme(), before, (match) -> {
            final WindShear ws = new WindShearImpl();
            List<String> runways = new ArrayList<>();
            while (match != null) {
                String rw = match.getParsedValue(Lexeme.ParsedValueName.RUNWAY, String.class);
                if ("ALL".equals(rw)) {
                    ws.setAllRunways(true);
                } else if (rw != null) {
                    runways.add(rw);
                }
                match = findNext(WIND_SHEAR, match, before);
            }
            if (!runways.isEmpty()) {
                ws.setRunwayDirectionDesignators(runways);
            }
            msg.setWindShear(ws);
        });
    }

    private static void updateSeaState(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(SEA_STATE, lexed.getFirstLexeme(), before, (match) -> {
            SeaState ss = new SeaStateImpl();
            Object[] values = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Object[].class);
            if (values[0] instanceof Integer) {
                String tempUnit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
                ss.setSeaSurfaceTemperature(new NumericMeasureImpl((Integer) values[0], tempUnit));
            }
            if (values[1] instanceof fi.fmi.avi.parser.impl.lexer.token.SeaState.SeaSurfaceState) {
                switch ((fi.fmi.avi.parser.impl.lexer.token.SeaState.SeaSurfaceState) values[1]) {
                    case CALM_GLASSY:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.CALM_GLASSY);
                        break;
                    case CALM_RIPPLED:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.CALM_RIPPLED);
                        break;
                    case SMOOTH_WAVELETS:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.SMOOTH_WAVELETS);
                        break;
                    case SLIGHT:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.SLIGHT);
                        break;
                    case MODERATE:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.MODERATE);
                        break;
                    case ROUGH:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.ROUGH);
                        break;
                    case VERY_ROUGH:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.VERY_ROUGH);
                        break;
                    case HIGH:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.HIGH);
                        break;
                    case VERY_HIGH:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.VERY_HIGH);
                        break;
                    case PHENOMENAL:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.PHENOMENAL);
                        break;
                    case MISSING:
                        ss.setSeaSurfaceState(AviationCodeListUser.SeaSurfaceState.MISSING_VALUE);
                        break;
                }
            }
            if (values[3] instanceof Integer) {
                String heightUnit = match.getParsedValue(Lexeme.ParsedValueName.UNIT2, String.class);
                ss.setSignificantWaveHeight(new NumericMeasureImpl((Integer) values[3], heightUnit));
            }
        });
    }

    private static void updateRunwayStates(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(RUNWAY_STATE, lexed.getFirstLexeme(), before, (match) -> {
        	//TODO
        });
    }

    private static void updateTrends(final Metar msg, final LexemeSequence lexed) throws ParsingException {
        Lexeme.Identity[] before = { REMARKS_START };
        findNext(FORECAST_CHANGE_INDICATOR, lexed.getFirstLexeme(), before, (match) -> {
        	//TODO
        });
    }
    
    private static void updateRemarks(final Metar msg, final LexemeSequence lexed) throws ParsingException {
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
    

}
