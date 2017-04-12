package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.*;

import java.util.ArrayList;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.data.AviationCodeListUser.CloudAmount;
import fi.fmi.avi.data.AviationCodeListUser.MetarStatus;

import fi.fmi.avi.data.AviationCodeListUser.RelationalOperator;
import fi.fmi.avi.data.AviationCodeListUser.VisualRangeTendency;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudLayer;
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
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.Lexeme.ParsedValueName;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingException;
import fi.fmi.avi.parser.ParsingException.Type;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.token.SurfaceWind;
import fi.fmi.avi.parser.impl.lexer.token.AtmosphericPressureQNH.PressureMeasurementUnit;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer.CloudCover;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer.CloudType;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer.SpecialValue;
import fi.fmi.avi.parser.impl.lexer.token.MetricHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.Weather.WeatherCodePart;

/**
 * Created by rinne on 13/12/16.
 */
public class AviMessageParserImpl implements AviMessageParser {
    private static final Logger LOG = LoggerFactory.getLogger(AviMessageParserImpl.class);

    @Override
    public <T extends AviationWeatherMessage> T parseMessage(final LexemeSequence lexed, final Class<T> type) throws ParsingException {
        return parseMessage(lexed, type, null);
    }

    @Override
    public <T extends AviationWeatherMessage> T parseMessage(final LexemeSequence lexed, final Class<T> type, final ParsingHints hints) 
            throws ParsingException {
        LOG.info(lexed.getFirstLexeme().getStatus().toString());
        if (Lexeme.Status.OK == lexed.getFirstLexeme().getStatus()) {
            if (Metar.class.isAssignableFrom(type)) {
                if (METAR_START == lexed.getFirstLexeme().getIdentity()) {
                    return (T) parseMetar(lexed, hints);
                } else {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "The first lexeme is not METAR start token");
                }
            } else if (TAF.class.isAssignableFrom(type)) {
                if (TAF_START == lexed.getFirstLexeme().getIdentity()) {
                    return (T) parseTAF(lexed, hints);
                } else {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "The first lexeme is not TAF start token");
                }
            }
        }
        throw new IllegalArgumentException("Unable to parse messsage of type " + type.getCanonicalName());
    }

    @SuppressWarnings("incomplete-switch")
	private static Metar parseMetar(final LexemeSequence lexed, final ParsingHints hints) throws ParsingException {
        Metar retval = new MetarImpl();
        Lexeme l = lexed.getFirstLexeme();
        
        if (findForwards(CORRECTION, l, Identity.AERODROME_DESIGNATOR) != null){
        	retval.setStatus(MetarStatus.CORRECTION);
        } else {
        	retval.setStatus(MetarStatus.NORMAL);
        }

        l = findForwards(AERODROME_DESIGNATOR, l, ISSUE_TIME);
        if (l != null){
        	retval.setAerodromeDesignator(l.getParsedValue(ParsedValueName.VALUE, String.class));
        } else {
        	throw new ParsingException(Type.SYNTAX_ERROR, "Aerodrome designator not given");
        }
        
        updateMetarIssueTime(retval, l, AERODROME_DESIGNATOR);
		
		
		l = findForwards(SURFACE_WIND, l, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		if (l != null) {
			Object direction = l.getParsedValue(ParsedValueName.DIRECTION, Integer.class);
			Integer meanSpeed = l.getParsedValue(ParsedValueName.DIRECTION, Integer.class);
			Integer gust = l.getParsedValue(ParsedValueName.DIRECTION, Integer.class);
			String unit = l.getParsedValue(ParsedValueName.UNIT, String.class);
			
			ObservedSurfaceWind wind = retval.getSurfaceWind();
			if (wind == null) {
				wind = new ObservedSurfaceWindImpl();
			}
			
			if (direction == SurfaceWind.WindDirection.VARIABLE) {
				wind.setVariableDirection(true);
			} else if (direction != null && direction instanceof Integer) {
				wind.setMeanWindDirection(new NumericMeasureImpl((Integer)direction, "deg"));
			} else {
				throw new ParsingException(Type.MISSING_DATA, "Direction missing for surface wind");
			}
			
			if (meanSpeed != null) {
				wind.setMeanWindSpeed(new NumericMeasureImpl(meanSpeed, unit));
			} else {
				throw new ParsingException(Type.MISSING_DATA, "Mean speed missing for surface wind");
			}
			
			if (gust != null) {
				wind.setWindGust(new NumericMeasureImpl(gust, unit));
			}
			
			l = findForwards(VARIABLE_WIND_DIRECTION, l, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
			if (l != null) {
				Integer maxDirection = l.getParsedValue(ParsedValueName.MAX_DIRECTION, Integer.class);
				Integer minDirection = l.getParsedValue(ParsedValueName.MIN_DIRECTION, Integer.class);

				if (minDirection != null) {
					wind.setExtremeCounterClockwiseWindDirection(new NumericMeasureImpl(minDirection, "deg"));
				}
				if (maxDirection != null) {
					wind.setExtremeClockwiseWindDirection(new NumericMeasureImpl(maxDirection, "deg"));
				}
			}
			retval.setSurfaceWind(wind);
		} else {
			//TODO: cases where a missing surface wind is acceptable, otherwise throw exception
		}
		
		l = findForwards(CAVOK, lexed.getFirstLexeme(), HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		if (l != null) {
			retval.setCeilingAndVisibilityOk(true);
		}
		
		l = findForwards(HORIZONTAL_VISIBILITY, lexed.getFirstLexeme(), CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		if (l != null) {
			MetricHorizontalVisibility.DirectionValue direction = l.getParsedValue(ParsedValueName.DIRECTION, MetricHorizontalVisibility.DirectionValue.class);
			String unit = l.getParsedValue(ParsedValueName.UNIT, String.class);
			Integer value = l.getParsedValue(ParsedValueName.VALUE, Integer.class);
			RecognizingAviMessageTokenLexer.RelationalOperator operator = l.getParsedValue(ParsedValueName.RELATIONAL_OPERATOR, RecognizingAviMessageTokenLexer.RelationalOperator.class);
			
			HorizontalVisibility vis = new HorizontalVisibilityImpl();
			if (direction != null) {
				vis.setMinimumVisibility(new NumericMeasureImpl(value, unit));
				vis.setMinimumVisibilityDirection(new NumericMeasureImpl(direction.inDegrees(), "deg"));
			} else {
				vis.setPrevailingVisibility(new NumericMeasureImpl(value, unit));
				if (RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN == operator) {
					vis.setPrevailingVisibilityOperator(RelationalOperator.BELOW);
				} else if (RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN == operator) {
					vis.setPrevailingVisibilityOperator(RelationalOperator.ABOVE);
				}
			}
			retval.setVisibility(vis);
		} else {
			//TODO: cases where a missing visibility is acceptable, otherwise throw exception
		}
		
		l = findForwards(RUNWAY_VISUAL_RANGE, l, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		List<RunwayVisualRange> rvrs = null;
		if (l != null) {
			rvrs = new ArrayList<RunwayVisualRange>();
		}
		while(l != null) {
			String runway = l.getParsedValue(ParsedValueName.RUNWAY, String.class);
			Integer minValue = l.getParsedValue(ParsedValueName.MIN_VALUE, Integer.class);
			RecognizingAviMessageTokenLexer.RelationalOperator minValueOperator = l.getParsedValue(ParsedValueName.RELATIONAL_OPERATOR, RecognizingAviMessageTokenLexer.RelationalOperator.class);
			Integer maxValue = l.getParsedValue(ParsedValueName.MAX_VALUE, Integer.class);
			RecognizingAviMessageTokenLexer.RelationalOperator maxValueOperator = l.getParsedValue(ParsedValueName.RELATIONAL_OPERATOR2, RecognizingAviMessageTokenLexer.RelationalOperator.class);
			RecognizingAviMessageTokenLexer.TendencyOperator tendencyIndicator = l.getParsedValue(ParsedValueName.TENDENCY_OPERATOR, RecognizingAviMessageTokenLexer.TendencyOperator.class);
			String unit = l.getParsedValue(ParsedValueName.UNIT, String.class);
			
			if (runway == null) {
				throw new ParsingException(Type.SYNTAX_ERROR, "Missing runway code for RVR in " + l.getTACToken());
			}
			if (minValue == null) {
				throw new ParsingException(Type.SYNTAX_ERROR, "Missing visibility value for RVR in " + l.getTACToken());
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
					rvr.setMeanRVROperator(RelationalOperator.BELOW);
				} else if (RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN == minValueOperator) {
					rvr.setMeanRVROperator(RelationalOperator.ABOVE);
				}
			}
			if (RecognizingAviMessageTokenLexer.TendencyOperator.DOWNWARD == tendencyIndicator) {
				rvr.setPastTendency(VisualRangeTendency.DOWNWARD);
			} else if (RecognizingAviMessageTokenLexer.TendencyOperator.UPWARD == tendencyIndicator) {
				rvr.setPastTendency(VisualRangeTendency.UPWARD);
			} else if (RecognizingAviMessageTokenLexer.TendencyOperator.NO_CHANGE == tendencyIndicator) {
				rvr.setPastTendency(VisualRangeTendency.NO_CHANGE);
			}
			rvrs.add(rvr);
			l = findForwards(RUNWAY_VISUAL_RANGE, l, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		}
		retval.setRunwayVisualRanges(rvrs);
		
		l = findForwards(WEATHER, lexed.getFirstLexeme(), CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		List<String> weather = null;
		if (l != null) {
			weather = new ArrayList<String>();
		}
		while(l != null) {
			@SuppressWarnings("unchecked")
			List<WeatherCodePart> codes = l.getParsedValue(ParsedValueName.VALUE, List.class);
			
			if (codes != null) {
				for (WeatherCodePart code:codes) {
					weather.add(code.getCode());
				}
			}
			l = findForwards(WEATHER, l, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		}
		if (weather != null) {
			retval.setPresentWeatherCodes(weather);
		}
		
		l = findForwards(CLOUD, lexed.getFirstLexeme(), AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		ObservedClouds clouds = null;
		List<fi.fmi.avi.data.CloudLayer> layers = null;
		if (l != null) {
			clouds = new ObservedCloudsImpl();
			layers = new ArrayList<fi.fmi.avi.data.CloudLayer>();
		}
		while(l != null) {
			CloudCover cover = l.getParsedValue(ParsedValueName.COVER, CloudCover.class);
			CloudType type = l.getParsedValue(ParsedValueName.TYPE, CloudType.class);
			Object value = l.getParsedValue(ParsedValueName.VALUE);
			String unit = l.getParsedValue(ParsedValueName.UNIT, String.class);
			
			if (SpecialValue.AMOUNT_AND_HEIGHT_UNOBSERVABLE_BY_AUTO_SYSTEM == value) {
				clouds.setAmountAndHeightUnobservableByAutoSystem(true);
			} else if (value instanceof Integer){
				Integer height = (Integer)value;
				if (CloudCover.SKY_OBSCURED == cover) {
					clouds.setVerticalVisibility(new NumericMeasureImpl(height, unit));
				} else {
					CloudLayer layer = new CloudLayerImpl();
					switch(cover) { 
					case FEW:
						layer.setAmount(CloudAmount.FEW);
						break;
					case SCATTERED:
						layer.setAmount(CloudAmount.SCT);
						break;
					case BROKEN:
						layer.setAmount(CloudAmount.BKN);
						break;
					case OVERCAST:
						layer.setAmount(CloudAmount.OVC);
						break;
					case SKY_OBSCURED:
					case NO_SIG_CLOUDS:
					case NO_LOW_CLOUDS:
						//NOOP
					}
					if (CloudType.TOWERING_CUMULUS == type) {
						layer.setCloudType(fi.fmi.avi.data.AviationCodeListUser.CloudType.TCU);
					} else if (CloudType.CUMULONIMBUS == type) {
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
				throw new ParsingException(Type.SYNTAX_ERROR, "Cloud layer height is not an integer in " + l.getTACToken());
			}
			
			l = findForwards(CLOUD, l, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		}
		if (layers != null && clouds != null) {
			clouds.setLayers(layers);
		}
		retval.setClouds(clouds);
		
		l = findForwards(AIR_DEWPOINT_TEMPERATURE, lexed.getFirstLexeme(), AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		if (l != null) {
			String unit = l.getParsedValue(ParsedValueName.UNIT, String.class);
			Integer[] values = l.getParsedValue(ParsedValueName.VALUE, Integer[].class);
			if (values[0] != null) {
				retval.setAirTemperature(new NumericMeasureImpl(values[0],unit));
			} else {
				throw new ParsingException(Type.MISSING_DATA, "Missing air temperature value");
			}
			if (values[1] != null) {
				retval.setDewpointTemperature(new NumericMeasureImpl(values[1],unit));
			} else {
				throw new ParsingException(Type.MISSING_DATA, "Missing dewpoint temperature value");
			}
		} else {
			throw new ParsingException(Type.MISSING_DATA, "Missing air temperature and dewpoint temperature values");
		}
		
		l = findForwards(AIR_PRESSURE_QNH, l, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		if (l != null) {
			PressureMeasurementUnit unit = l.getParsedValue(ParsedValueName.UNIT, PressureMeasurementUnit.class);
			Integer value = l.getParsedValue(ParsedValueName.VALUE, Integer.class);
			if (value != null) {
				String unitStr = "";
				if (unit == PressureMeasurementUnit.HECTOPASCAL) {
					unitStr = "hPa";
				} else if (unit == PressureMeasurementUnit.INCHES_OF_MERCURY) {
					unitStr = "in Hg";
				}
				retval.setAltimeterSettingQNH(new NumericMeasureImpl(value, unitStr));
			} else {
				throw new ParsingException(Type.MISSING_DATA, "Missing air pressure value");
			}
		} else {
			throw new ParsingException(Type.SYNTAX_ERROR, "QNH missing");
		}
		
		l = findForwards(RECENT_WEATHER, l, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		weather = null;
		if (l != null) {
			weather = new ArrayList<String>();
		}
		while(l != null) {
			@SuppressWarnings("unchecked")
			List<WeatherCodePart> codes = l.getParsedValue(ParsedValueName.VALUE, List.class);
			
			if (codes != null) {
				for (WeatherCodePart code:codes) {
					weather.add(code.getCode());
				}
			}
			l = findForwards(RECENT_WEATHER, l, FORECAST_CHANGE_INDICATOR, REMARKS_START);
		}
		if (weather != null) {
			retval.setRecentWeatherCodes(weather);
		}
		
		//TODO: Wind shear
		//TODO: sea state
		//TODO: runway states
		//TODO: trends
		
        return retval;
    }
    
    private static void updateMetarIssueTime(final Metar msg, final Lexeme from, final Identity...before) throws ParsingException {
    	Lexeme l = findForwards(ISSUE_TIME, from, before);
		if (l != null) {
			Integer day = l.getParsedValue(ParsedValueName.DAY1, Integer.class);
			Integer minute = l.getParsedValue(ParsedValueName.MINUTE1, Integer.class);
			Integer hour = l.getParsedValue(ParsedValueName.HOUR1, Integer.class);
			if (day != null && minute != null && hour != null) {
				msg.setIssueDayOfMonth(day);
				msg.setIssueHour(hour);
				msg.setIssueMinute(minute);
				msg.setIssueTimeZone("UTC");
			}
		} else {
			throw new ParsingException(Type.MISSING_DATA, "Missing at least some of the issue time components");
		}
    }
    
    private static Lexeme findForwards(final Identity needle, final Lexeme from, final Identity...stopAt) {
    	Lexeme retval = null;
    	Lexeme current = from.getNext();
    	if (current == null) {
    		return null;
    	}
    	boolean stop = false;
    	Identity currentId;
    	while (!stop) {
    		currentId = current.getIdentityIfAcceptable();
    		if (currentId == needle) {
    			retval = current;
    		}
    		stop = !current.hasNext() || retval != null;
    		if (stopAt != null) {
    			for (Identity i:stopAt) {
    				if (i == currentId) {
    					stop = true;
    					break;
    				}
    			}
    		}
    		current = current.getNext();
    	}
    	return retval;
    }
    
	private static TAF parseTAF(final LexemeSequence lexed, final ParsingHints hints) throws ParsingException {
        TAFImpl retval = new TAFImpl();
        updateBaseForecast(retval, lexed, hints);
        updateChangeForecasts(retval, lexed, hints);
        return retval;
    }

    private static void updateBaseForecast(final TAFImpl taf, final LexemeSequence lexed, final ParsingHints hints) {
        //TODO
    }

    private static void updateChangeForecasts(final TAFImpl taf, final LexemeSequence lexed, final ParsingHints hints) {
        //TODO
    }


}
