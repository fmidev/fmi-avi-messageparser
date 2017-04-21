package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity;
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
import static fi.fmi.avi.parser.Lexeme.Identity.REMARK;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_VISUAL_RANGE;
import static fi.fmi.avi.parser.Lexeme.Identity.SEA_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.VARIABLE_WIND_DIRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.WIND_SHEAR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationCodeListUser.WeatherCodeIntensity;
import fi.fmi.avi.data.AviationCodeListUser.WeatherCodeKind;
import fi.fmi.avi.data.impl.CloudLayerImpl;
import fi.fmi.avi.data.impl.NumericMeasureImpl;
import fi.fmi.avi.data.impl.WeatherImpl;
import fi.fmi.avi.data.metar.HorizontalVisibility;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.ObservedSurfaceWind;
import fi.fmi.avi.data.metar.RunwayState;
import fi.fmi.avi.data.metar.RunwayVisualRange;
import fi.fmi.avi.data.metar.SeaState;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.metar.TrendTimeGroups;
import fi.fmi.avi.data.metar.WindShear;
import fi.fmi.avi.data.metar.impl.HorizontalVisibilityImpl;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.data.metar.impl.ObservedCloudsImpl;
import fi.fmi.avi.data.metar.impl.ObservedSurfaceWindImpl;
import fi.fmi.avi.data.metar.impl.RunwayStateImpl;
import fi.fmi.avi.data.metar.impl.RunwayVisualRangeImpl;
import fi.fmi.avi.data.metar.impl.SeaStateImpl;
import fi.fmi.avi.data.metar.impl.TrendForecastImpl;
import fi.fmi.avi.data.metar.impl.TrendTimeGroupsImpl;
import fi.fmi.avi.data.metar.impl.WindShearImpl;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.ParsedValueName;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingIssue.Type;
import fi.fmi.avi.parser.ParsingResult;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.token.AtmosphericPressureQNH;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer;
import fi.fmi.avi.parser.impl.lexer.token.ForecastChangeIndicator;
import fi.fmi.avi.parser.impl.lexer.token.MetricHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.RunwayState.RunwayStateContamination;
import fi.fmi.avi.parser.impl.lexer.token.RunwayState.RunwayStateDeposit;
import fi.fmi.avi.parser.impl.lexer.token.RunwayState.RunwayStateReportSpecialValue;
import fi.fmi.avi.parser.impl.lexer.token.RunwayState.RunwayStateReportType;
import fi.fmi.avi.parser.impl.lexer.token.SurfaceWind;
import fi.fmi.avi.parser.impl.lexer.token.Weather;

/**
 * Created by rinne on 13/04/17.
 */
//TODO: check for each result.addIssue call if it makes to continue or return:
public class MetarParserImpl extends AbstractAviMessageParser implements AviMessageSpecificParser<Metar> {

    private static final Logger LOG = LoggerFactory.getLogger(MetarParserImpl.class);

    private static Identity[] zeroOrOneAllowed = { AERODROME_DESIGNATOR, ISSUE_TIME, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, WIND_SHEAR, SEA_STATE,
            REMARKS_START };

    public ParsingResult<Metar> parseMessage(final LexemeSequence lexed, final ParsingHints hints) {
        ParsingResult<Metar> result = new ParsingResultImpl<>();
        boolean[] oneFound = new boolean[zeroOrOneAllowed.length];
        Iterator<Lexeme> it = lexed.getRecognizedLexemes();
        while (it.hasNext()) {
            Lexeme l = it.next();
            for (int i = 0; i < zeroOrOneAllowed.length; i++) {
                if (zeroOrOneAllowed[i] == l.getIdentity()) {
                    if (!oneFound[i]) {
                        oneFound[i] = true;
                    } else {
                        result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "More than one of " + l.getIdentity() + " in " + lexed.getTAC()));
                    }
                }
            }
        }
        result.setParsedMessage(new MetarImpl());

        Identity[] stopAt = { AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CORRECTION, lexed.getFirstLexeme(), stopAt, (match) -> result.getParsedMessage().setStatus(AviationCodeListUser.MetarStatus.CORRECTION),
                () -> result.getParsedMessage().setStatus(AviationCodeListUser.MetarStatus.NORMAL));

        stopAt = new Identity[] { ISSUE_TIME, SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER,
                WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(AERODROME_DESIGNATOR, lexed.getFirstLexeme(), stopAt,
                (match) -> result.getParsedMessage().setAerodromeDesignator(match.getParsedValue(Lexeme.ParsedValueName.VALUE, String.class)), () -> {
                    result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Aerodrome designator not given in " + lexed.getTAC()));
                });

        updateMetarIssueTime(result, lexed, hints);
        updateSurfaceWind(result, lexed, hints);

        stopAt = new Identity[] { HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR,
                SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(CAVOK, lexed.getFirstLexeme(), stopAt, (match) -> result.getParsedMessage().setCeilingAndVisibilityOk(true));

        updateHorizontalVisibility(result, lexed, hints);
        updateRVR(result, lexed, hints);
        updatePresentWeather(result, lexed, hints);
        updateClouds(result, lexed, hints);
        updateTemperatures(result, lexed, hints);
        updateQNH(result, lexed, hints);
        updateRecentWeather(result, lexed, hints);
        updateWindShear(result, lexed, hints);
        updateSeaState(result, lexed, hints);
        updateRunwayStates(result, lexed, hints);
        updateTrends(result, lexed, hints);
        updateRemarks(result, lexed, hints);
        return result;
    }

    private static void updateMetarIssueTime(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
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
            result.addIssue(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing at least some of the issue time components in " + lexed.getTAC()));
        });

    }

    private static void updateSurfaceWind(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Identity[] before = { CAVOK, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR,
                SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(SURFACE_WIND, lexed.getFirstLexeme(), before, (match) -> {
            Object direction = match.getParsedValue(Lexeme.ParsedValueName.DIRECTION, Integer.class);
            Integer meanSpeed = match.getParsedValue(Lexeme.ParsedValueName.MEAN_VALUE, Integer.class);
            Integer gust = match.getParsedValue(Lexeme.ParsedValueName.MAX_VALUE, Integer.class);
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);

            final ObservedSurfaceWind wind = new ObservedSurfaceWindImpl();

            if (direction == SurfaceWind.WindDirection.VARIABLE) {
                wind.setVariableDirection(true);
            } else if (direction != null && direction instanceof Integer) {
                wind.setMeanWindDirection(new NumericMeasureImpl((Integer) direction, "deg"));
            } else {
                result.addIssue(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Direction missing for surface wind:" + match.getTACToken()));
            }

            if (meanSpeed != null) {
                wind.setMeanWindSpeed(new NumericMeasureImpl(meanSpeed, unit));
            } else {
                result.addIssue(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Mean speed missing for surface wind:" + match.getTACToken()));
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
            result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing surface wind information in " + lexed.getTAC()));
        });
    }

    private static void updateHorizontalVisibility(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
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
            result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing horizontal visibility information in " + lexed.getTAC()));
        });
    }

    private static void updateRVR(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
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
                    result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing runway code for RVR in " + match.getTACToken()));
                }
                if (minValue == null) {
                    result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Missing visibility value for RVR in " + match.getTACToken()));
                }
                RunwayVisualRange rvr = new RunwayVisualRangeImpl();
                rvr.setRunwayDirectionDesignator(runway);
                if (maxValue != null) {
                	rvr.setVaryingRVRMinimum(new NumericMeasureImpl(minValue, unit));
                    if (RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN == minValueOperator) {
                    	rvr.setVaryingRVRMinimumOperator(AviationCodeListUser.RelationalOperator.BELOW);
                    } else if (RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN == minValueOperator) {
                    	rvr.setVaryingRVRMinimumOperator(AviationCodeListUser.RelationalOperator.ABOVE);	
                    }
                    
                    rvr.setVaryingRVRMaximum(new NumericMeasureImpl(maxValue, unit));
                    if (RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN == maxValueOperator) {
                    	rvr.setVaryingRVRMaximumOperator(AviationCodeListUser.RelationalOperator.BELOW);
                    } else if (RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN == maxValueOperator) {
                    	rvr.setVaryingRVRMaximumOperator(AviationCodeListUser.RelationalOperator.ABOVE);	
                    }                    
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

    private static void updatePresentWeather(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Identity[] before = { CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR,
                REMARKS_START };
        Lexeme match = findNext(WEATHER, lexed.getFirstLexeme(), before);
        if (match != null) {
            List<fi.fmi.avi.data.Weather> weather = new ArrayList<>();
            result.addIssue(appendWeatherCodes(match, weather, before, hints));
            if (!weather.isEmpty()) {
                msg.setPresentWeather(weather);
            }
        }
    }

    private static List<ParsingIssue> appendWeatherCodes(final Lexeme source, List<fi.fmi.avi.data.Weather> target, Identity[] before,
            final ParsingHints hints) {
        Lexeme l = source;
        List<ParsingIssue> issues = new ArrayList<>();
        while (l != null) {
            @SuppressWarnings("unchecked")
            List<Weather.WeatherCodePart> codeParts = l.getParsedValue(Lexeme.ParsedValueName.VALUE, List.class);
            if (codeParts != null) {
            	fi.fmi.avi.data.Weather weather = new WeatherImpl();
                for (Weather.WeatherCodePart code : codeParts) {
                 switch (code) {
					case HIGH_INTENSITY:
						weather.setIntensity(WeatherCodeIntensity.HIGH);
						break;
					case IN_VICINITY:
						weather.setInVicinity(true);
						break;
					case LOW_INTENSITY:
						weather.setIntensity(WeatherCodeIntensity.LOW);
						break;
					default:
						WeatherCodeKind kind = WeatherCodeKind.forCode(code.getCode());
						if (kind != null) {
							weather.setKind(kind);
						} else {
                            issues.add(new ParsingIssue(Type.SYNTAX_ERROR, "Unknown weather code " + code.getCode() + " in " + l.getTACToken()));
                        }
						break;
                 	} 
                }
                target.add(weather);
            }
            l = findNext(WEATHER, l, before);
        }
        return issues;
    }

    private static void updateClouds(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
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
                            case SKY_CLEAR:
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
                    result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Cloud layer height is not an integer in " + match.getTACToken()));
                }

                match = findNext(CLOUD, match, before);
            }
            if (!layers.isEmpty()) {
                clouds.setLayers(layers);
            }
            msg.setClouds(clouds);
        });

    }

    private static void updateTemperatures(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Identity[] before = { AIR_PRESSURE_QNH, RECENT_WEATHER, WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(AIR_DEWPOINT_TEMPERATURE, lexed.getFirstLexeme(), before, (match) -> {
            String unit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
            Integer[] values = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Integer[].class);
            if (values[0] != null) {
                msg.setAirTemperature(new NumericMeasureImpl(values[0], unit));
            } else {
                result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing air temperature value in " + match.getTACToken()));
            }
            if (values[1] != null) {
                msg.setDewpointTemperature(new NumericMeasureImpl(values[1], unit));
            } else {
                result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing dewpoint temperature value in " + match.getTACToken()));
            }
        }, () -> {
            result.addIssue(new ParsingIssue(Type.MISSING_DATA, "Missing air temperature and dewpoint temperature values in " + lexed.getTAC()));
        });

    }

    private static void updateQNH(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
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
                } else {
                    result.addIssue(
                            new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "Unknown unit for air pressure: " + unitStr + " in " + match.getTACToken()));
                }
                msg.setAltimeterSettingQNH(new NumericMeasureImpl(value, unitStr));
            } else {
                result.addIssue(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing air pressure value: " + match.getTACToken()));
            }
        }, () -> {
            result.addIssue(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "QNH missing in " + lexed.getTAC()));
        });
    }

    private static void updateRecentWeather(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Identity[] before = { WIND_SHEAR, SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        Lexeme match = findNext(RECENT_WEATHER, lexed.getFirstLexeme(), before);
        if (match != null) {
            List<fi.fmi.avi.data.Weather> weather = new ArrayList<>();
            result.addIssue(appendWeatherCodes(match, weather, before, hints));
            if (!weather.isEmpty()) {
                msg.setRecentWeather(weather);
            }
        }
    }

    private static void updateWindShear(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Identity[] before = { SEA_STATE, RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(WIND_SHEAR, lexed.getFirstLexeme(), before, (match) -> {
            final WindShear ws = new WindShearImpl();
            List<String> runways = new ArrayList<>();
            while (match != null) {
                String rw = match.getParsedValue(Lexeme.ParsedValueName.RUNWAY, String.class);
                if ("ALL".equals(rw)) {
                	if (!runways.isEmpty()) {
                        result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR,
                                "Wind shear reported both to all runways and at least one specific runway: " + match.getTACToken()));
                    } else {
                        ws.setAllRunways(true);
                    }
                } else if (rw != null) {
                	if (ws.isAllRunways()) {
                        result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR,
                                "Wind shear reported both to all runways and at least one specific runway:" + match.getTACToken()));
                    } else {
                        runways.add(rw);
                    }
                }
                match = findNext(WIND_SHEAR, match, before);
            }
            if (!runways.isEmpty()) {
                ws.setRunwayDirectionDesignators(runways);
            }
            msg.setWindShear(ws);
        });
    }

    private static void updateSeaState(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Lexeme.Identity[] before = { RUNWAY_STATE, FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(SEA_STATE, lexed.getFirstLexeme(), before, (match) -> {
            SeaState ss = new SeaStateImpl();
            Object[] values = match.getParsedValue(Lexeme.ParsedValueName.VALUE, Object[].class);
            if (values[0] instanceof Integer) {
                String tempUnit = match.getParsedValue(Lexeme.ParsedValueName.UNIT, String.class);
                ss.setSeaSurfaceTemperature(new NumericMeasureImpl((Integer) values[0], tempUnit));
            }
            if (values[1] instanceof fi.fmi.avi.parser.impl.lexer.token.SeaState.SeaSurfaceState) {
                if (values[2] != null) {
                    result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR,
                            "Sea state cannot contain both sea surface state and significant wave height:" + match.getTACToken()));
                } else {
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
            }
            if (values[2] instanceof Integer) {
            	if (values[1] != null) {
                    result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR,
                            "Sea state cannot contain both sea surface state and significant wave height:" + match.getTACToken()));
                } else {
                    String heightUnit = match.getParsedValue(Lexeme.ParsedValueName.UNIT2, String.class);
                    ss.setSignificantWaveHeight(new NumericMeasureImpl((Integer) values[2], heightUnit));
                }
            }
            msg.setSeaState(ss);
        });
    }

    private static void updateRunwayStates(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Lexeme.Identity[] before = { FORECAST_CHANGE_INDICATOR, REMARKS_START };
        findNext(RUNWAY_STATE, lexed.getFirstLexeme(), before, (match) -> {
        	List<RunwayState> states = new ArrayList<>();
        	while (match != null){
        		RunwayStateImpl rws = new RunwayStateImpl();
	        	@SuppressWarnings("unchecked")
				Map<RunwayStateReportType, Object> values = match.getParsedValue(ParsedValueName.VALUE, Map.class);
	        	Boolean repetition = (Boolean)values.get(RunwayStateReportType.REPETITION);
	        	Boolean allRunways = (Boolean)values.get(RunwayStateReportType.ALL_RUNWAYS);
	        	String runway = match.getParsedValue(ParsedValueName.RUNWAY, String.class);
	        	RunwayStateDeposit deposit = (RunwayStateDeposit)values.get(RunwayStateReportType.DEPOSITS);
	        	RunwayStateContamination contamination = (RunwayStateContamination)values.get(RunwayStateReportType.CONTAMINATION);
	        	Integer depthOfDeposit = (Integer)values.get(RunwayStateReportType.DEPTH_OF_DEPOSIT);
	        	String unitOfDeposit = (String)values.get(RunwayStateReportType.UNIT_OF_DEPOSIT);
	        	RunwayStateReportSpecialValue depthModifier = (RunwayStateReportSpecialValue)values.get(RunwayStateReportType.DEPTH_MODIFIER);
	        	Boolean cleared = (Boolean)values.get(RunwayStateReportType.CLEARED);
	        	
	        	if (repetition != null && repetition) {
	        		rws.setRepetition(true);
	        	} else if (allRunways != null && allRunways) {
	        		rws.setAllRunways(true);
	        	} else if (runway != null) {
	        		rws.setRunwayDirectionDesignator(runway);
	        	} else {
                    result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "No runway specified for runway state report: " + match.getTACToken()));
                }
	        	if (deposit != null) {
		        	switch(deposit) {
					case CLEAR_AND_DRY:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.CLEAR_AND_DRY);
						break;
					case COMPACTED_OR_ROLLED_SNOW:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.COMPACT_OR_ROLLED_SNOW);
						break;
					case DAMP:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.DAMP);
						break;
					case DRY_SNOW:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.DRY_SNOW);
						break;
					case FROZEN_RUTS_OR_RIDGES:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.FROZEN_RUTS_OR_RIDGES);
						break;
					case ICE:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.ICE);
						break;
					case NOT_REPORTED:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.MISSING_OR_NOT_REPORTED);
						break;
					case RIME_OR_FROST_COVERED:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.RIME_AND_FROST_COVERED);
						break;
					case SLUSH:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.SLUSH);
						break;
					case WET:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.WET_WITH_WATER_PATCHES);
						break;
					case WET_SNOW:
						rws.setDeposit(AviationCodeListUser.RunwayDeposit.WET_SNOW);
						break;
		        	}
	        	}
	        	
	        	if (contamination != null) {
	        		switch(contamination) {
	        		case LESS_OR_EQUAL_TO_10PCT:
						rws.setContamination(AviationCodeListUser.RunwayContamination.PCT_COVERED_LESS_THAN_10);
						break;
					case FROM_11_TO_25PCT:
						rws.setContamination(AviationCodeListUser.RunwayContamination.PCT_COVERED_11_25);
						break;
					case FROM_26_TO_50PCT:
						rws.setContamination(AviationCodeListUser.RunwayContamination.PCT_COVERED_26_50);
						break;
					case FROM_51_TO_100PCT:
						rws.setContamination(AviationCodeListUser.RunwayContamination.PCT_COVERED_51_100);
						break;
					case NOT_REPORTED:
						rws.setContamination(AviationCodeListUser.RunwayContamination.MISSING_OR_NOT_REPORTED);
						break;
	        		}
	        	}
	        	
	        	if (depthOfDeposit != null) {
	        		if (deposit == null) {
                        result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR, "Missing deposit kind but depth given for runway state: " + match.getTACToken()));
                    } else {
                        rws.setDepthOfDeposit(new NumericMeasureImpl(depthOfDeposit, unitOfDeposit));
                    }
                }
	        	
	        	if (depthModifier != null) {
	        		if (depthOfDeposit == null) {
                        result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR,
                                "Missing deposit depth but depth modifier given for runway state: " + match.getTACToken()));
                    } else {
                        switch (depthModifier) {
                            case LESS_THAN_OR_EQUAL:
                                rws.setDepthOperator(AviationCodeListUser.RelationalOperator.BELOW);
                                break;
                            case MEASUREMENT_UNRELIABLE:
                            case NOT_MEASURABLE:
                                result.addIssue(
                                        new ParsingIssue(Type.SYNTAX_ERROR, "Illegal modifier for depth of deposit for runway state:" + match.getTACToken()));
                                break;
                            case MORE_THAN_OR_EQUAL:
                                rws.setDepthOperator(AviationCodeListUser.RelationalOperator.ABOVE);
                                break;
                            case RUNWAY_NOT_OPERATIONAL:
                                rws.setRunwayNotOperational(true);
                                break;
                        }
                    }
                }
	        	if (cleared != null && cleared) {
	        		if (deposit != null || contamination != null || depthOfDeposit != null) {
                        result.addIssue(new ParsingIssue(Type.LOGICAL_ERROR,
                                "Runway state cannot be both cleared and contain deposit or contamination info: " + match.getTACToken()));
                    } else {
                        rws.setCleared(true);
                    }
                }
	        	states.add(rws);
	        	match = findNext(RUNWAY_STATE, match, before);
        	}
        	if (!states.isEmpty()) {
        		msg.setRunwayStates(states);
        	}
        });
    }

    private static void updateTrends(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
        Lexeme.Identity[] before = { REMARKS_START };
        findNext(FORECAST_CHANGE_INDICATOR, lexed.getFirstLexeme(), before, (changeFct) -> {
            List<TrendForecast> trends = new ArrayList<>();
            //loop over change forecasts:
            Lexeme.Identity[] stopWithingGroup = { FORECAST_CHANGE_INDICATOR, REMARKS_START };
            while (changeFct != null) {
                TrendForecast fct = new TrendForecastImpl();
                Lexeme token = findNext(changeFct, stopWithingGroup);
                //loop over group tokens:
                while (token != null) {
                    TrendTimeGroups timeGroups = new TrendTimeGroupsImpl();
                    switch (token.getIdentity()) {
                        case CHANGE_FORECAST_TIME_GROUP: {
                            ForecastChangeIndicator.ForecastChangeIndicatorType type = token.getParsedValue(ParsedValueName.TYPE,
                                    ForecastChangeIndicator.ForecastChangeIndicatorType.class);
                            switch (type) {
                                case AT: {
                                    Integer fromHour = token.getParsedValue(ParsedValueName.HOUR1, Integer.class);
                                    Integer fromMinute = token.getParsedValue(ParsedValueName.MINUTE1, Integer.class);
                                    if (fromHour != null) {
                                        timeGroups.setFromHour(fromHour);
                                    } else {
                                        result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing hour from trend AT group " + token.getTACToken()));
                                    }
                                    if (fromMinute != null) {
                                        timeGroups.setFromMinute(fromMinute);
                                    } else {
                                        result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing minutes from trend AT group " + token.getTACToken()));
                                    }
                                    timeGroups.setSingleInstance(true);
                                    break;
                                }
                                case FROM: {
                                    Integer fromHour = token.getParsedValue(ParsedValueName.HOUR1, Integer.class);
                                    Integer fromMinute = token.getParsedValue(ParsedValueName.MINUTE1, Integer.class);
                                    if (fromHour != null) {
                                        timeGroups.setFromHour(fromHour);
                                    } else {
                                        result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing hour from trend FM group " + token.getTACToken()));
                                    }
                                    if (fromMinute != null) {
                                        timeGroups.setFromMinute(fromMinute);
                                    } else {
                                        result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing minutes from trend FM group " + token.getTACToken()));
                                    }
                                    break;
                                }
                                case UNTIL: {
                                    Integer toHour = token.getParsedValue(ParsedValueName.HOUR2, Integer.class);
                                    Integer toMinute = token.getParsedValue(ParsedValueName.MINUTE2, Integer.class);
                                    if (toHour != null) {
                                        timeGroups.setToHour(toHour);
                                    } else {
                                        result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing hour from trend TL group " + token.getTACToken()));
                                    }
                                    if (toMinute != null) {
                                        timeGroups.setFromMinute(toMinute);
                                    } else {
                                        result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "Missing minutes from trend TL group " + token.getTACToken()));
                                    }
                                    break;
                                }
                                case BECOMING:
                                    fct.setChangeIndicator(AviationCodeListUser.TrendForecastChangeIndicator.BECOMING);
                                    break;
                                case TEMPORARY_FLUCTUATIONS:
                                    fct.setChangeIndicator(AviationCodeListUser.TrendForecastChangeIndicator.TEMPORARY_FLUCTUATIONS);
                                    break;
                                case WITH_30_PCT_PROBABILITY:
                                case WITH_40_PCT_PROBABILITY:
                                    result.addIssue(new ParsingIssue(Type.SYNTAX_ERROR, "PROB groups not allowed in METAR " + token.getTACToken()));
                                default:
                                    break;
                            }

                            break;
                        }
                        case CAVOK:
                            fct.setCeilingAndVisibilityOk(true);
                            break;
                        case NO_SIGNIFICANT_WEATHER:
                            fct.setChangeIndicator(AviationCodeListUser.TrendForecastChangeIndicator.NO_SIGNIFICANT_CHANGES);
                            break;
                        case CLOUD:
                            //TODO
                            break;
                        case HORIZONTAL_VISIBILITY:
                            //TODO
                            break;
                        case SURFACE_WIND:
                            //TODO
                            break;
                        case WEATHER:
                            //TODO
                            break;
                        default:
                            break;
                    }
                    token = findNext(token, stopWithingGroup);
                }
                trends.add(fct);
                changeFct = findNext(FORECAST_CHANGE_INDICATOR, changeFct, before);
            }
            if (!trends.isEmpty()) {
                msg.setTrends(trends);
            }
        });
    }

    private static void updateRemarks(final ParsingResult<Metar> result, final LexemeSequence lexed, final ParsingHints hints) {
        final Metar msg = result.getParsedMessage();
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
