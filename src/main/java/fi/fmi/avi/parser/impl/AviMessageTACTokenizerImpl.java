package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.AMENDMENT;
import static fi.fmi.avi.parser.Lexeme.Identity.AUTOMATED;
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
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.RECENT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARK;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_VISUAL_RANGE;
import static fi.fmi.avi.parser.Lexeme.Identity.SEA_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.WIND_SHEAR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.CloudLayer;
import fi.fmi.avi.data.Weather;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.RunwayState;
import fi.fmi.avi.data.metar.RunwayVisualRange;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFAirTemperatureForecast;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.TACTokenReconstructor;

/**
 * Created by rinne on 15/02/17.
 */
public class AviMessageTACTokenizerImpl implements AviMessageTACTokenizer {

    private Map<Identity, TACTokenReconstructor> reconstructors = new HashMap<Identity, TACTokenReconstructor>();

    private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

    public void addReconstructor(final Identity id, TACTokenReconstructor reconstructor) {
        reconstructor.setLexingFactory(this.factory);
        this.reconstructors.put(id, reconstructor);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws TokenizingException {
        return this.tokenizeMessage(msg, null);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ParsingHints hints) throws TokenizingException {
        if (msg instanceof Metar) {
            return tokenizeMetar((Metar) msg, hints);
        } else if (msg instanceof TAF) {
            return tokenizeTAF((TAF) msg, hints);
        } else {
            throw new IllegalArgumentException("Do not know how to tokenize message of type " + msg.getClass().getCanonicalName());
        }
    }

    private LexemeSequence tokenizeMetar(final Metar msg, final ParsingHints hints) throws TokenizingException {
        LexemeSequenceBuilder retval = this.factory.createLexemeSequenceBuilder();
        appendToken(retval, METAR_START, msg, Metar.class, hints);
        appendToken(retval, CORRECTION, msg, Metar.class, hints);
        appendToken(retval, AERODROME_DESIGNATOR, msg, Metar.class, hints);
        appendToken(retval, ISSUE_TIME, msg, Metar.class, hints);
        appendToken(retval, AUTOMATED, msg, Metar.class, hints);
        appendToken(retval, SURFACE_WIND, msg, Metar.class, hints);
        appendToken(retval, CAVOK, msg, Metar.class, hints);
        appendToken(retval, HORIZONTAL_VISIBILITY, msg, Metar.class, hints);
        if (msg.getRunwayVisualRanges() != null) {
        	for (RunwayVisualRange range:msg.getRunwayVisualRanges()) {
        		appendToken(retval, RUNWAY_VISUAL_RANGE, msg, Metar.class,  hints, range);
        	}
        }
        if (msg.getPresentWeather() != null) {
        	for (Weather weather:msg.getPresentWeather()) {
        		 appendToken(retval, WEATHER, msg, Metar.class, hints, weather);
        	}
        }
        ObservedClouds obsClouds = msg.getClouds();
        if (obsClouds != null) {
            if (obsClouds.getVerticalVisibility() != null) {
                this.appendToken(retval, Identity.CLOUD, msg, Metar.class, hints, "VV");
            } else if (obsClouds.isAmountAndHeightUnobservableByAutoSystem()) {
            	this.appendToken(retval, Identity.CLOUD, msg, Metar.class, hints, "//////");
            } else {
                this.appendCloudLayers(retval, msg, Metar.class, obsClouds.getLayers(), hints);
            }
        }
        appendToken(retval, AIR_DEWPOINT_TEMPERATURE, msg, Metar.class, hints);
        appendToken(retval, AIR_PRESSURE_QNH, msg, Metar.class, hints);
        if (msg.getRecentWeather() != null) {
        	for (Weather weather:msg.getRecentWeather()) {
        		 appendToken(retval, RECENT_WEATHER, msg, Metar.class, hints, weather);
        	}
        }
        appendToken(retval, WIND_SHEAR, msg, Metar.class, hints);
        appendToken(retval, SEA_STATE, msg, Metar.class, hints);
        if (msg.getRunwayStates() != null) {
        	for (RunwayState state:msg.getRunwayStates()) {
        		appendToken(retval, RUNWAY_STATE, msg, Metar.class, hints, state);
        	}
        }
        appendToken(retval, NO_SIGNIFICANT_WEATHER, msg, Metar.class, hints);
        if (msg.getTrends() != null) {
            for (TrendForecast trend : msg.getTrends()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, msg, Metar.class, hints, trend);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, msg, Metar.class, hints, trend);
                appendToken(retval, SURFACE_WIND, msg, Metar.class, hints, trend);
                appendToken(retval, CAVOK, msg, Metar.class, hints, trend);
                appendToken(retval, HORIZONTAL_VISIBILITY, msg, Metar.class, hints, trend);
                if (trend.getForecastWeather() != null) {
                	for (Weather weather:trend.getForecastWeather()) {
                		 appendToken(retval, WEATHER, msg, Metar.class, hints, trend, weather);
                	}
                }
                CloudForecast clouds = trend.getCloud();
                if (clouds != null) {
	                if (clouds.getVerticalVisibility() != null) {
	                	this.appendToken(retval, Identity.CLOUD, msg, Metar.class, hints, "VV", trend);
	                } else {
	                    this.appendCloudLayers(retval, msg, Metar.class, clouds.getLayers(), hints, trend);
	                }
                }
            }
        }
        if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, msg, Metar.class, hints);
            for (String remark : msg.getRemarks()) {
                this.appendToken(retval, REMARK, msg, Metar.class, hints, remark);
            }
        }
        appendToken(retval, END_TOKEN, msg, Metar.class, hints);
        return retval.build();

    }

    private LexemeSequence tokenizeTAF(final TAF msg, final ParsingHints hints) throws TokenizingException {
        LexemeSequenceBuilder retval = this.factory.createLexemeSequenceBuilder();

        appendToken(retval, TAF_START, msg, TAF.class, hints);
        appendToken(retval, AMENDMENT, msg, TAF.class, hints);
        appendToken(retval, CORRECTION, msg, TAF.class, hints);
        appendToken(retval, AERODROME_DESIGNATOR, msg, TAF.class, hints);
        appendToken(retval, ISSUE_TIME, msg, TAF.class, hints);

        if (AviationCodeListUser.TAFStatus.MISSING != msg.getStatus()) {
            appendToken(retval, VALID_TIME, msg, TAF.class, hints);
            appendToken(retval, CANCELLATION, msg, TAF.class, hints);
            if (AviationCodeListUser.TAFStatus.CANCELLATION != msg.getStatus()) {
                TAFBaseForecast baseFct = msg.getBaseForecast();
                if (baseFct == null) {
                    throw new TokenizingException("Missing base forecast");
                }
                appendToken(retval, SURFACE_WIND, msg, TAF.class, hints, baseFct);
                appendToken(retval, CAVOK, msg, TAF.class, hints, baseFct);
                appendToken(retval, HORIZONTAL_VISIBILITY, msg, TAF.class, hints, baseFct);
                if (baseFct.getForecastWeather() != null) {
                    for (Weather weather : baseFct.getForecastWeather()) {
                        appendToken(retval, WEATHER, msg, TAF.class, hints, baseFct, weather);
                    }
                }
                CloudForecast clouds = baseFct.getCloud();
                if (clouds != null) {
                    if (clouds.getVerticalVisibility() != null) {
                        this.appendToken(retval, Identity.CLOUD, msg, TAF.class, hints, "VV", baseFct);
                    } else {
                        this.appendCloudLayers(retval, msg, TAF.class, clouds.getLayers(), hints, baseFct);
                    }
                }
                if (baseFct.getTemperatures() != null) {
                    for (TAFAirTemperatureForecast tempFct : baseFct.getTemperatures()) {
                        appendToken(retval, MAX_TEMPERATURE, msg, TAF.class, hints, baseFct, tempFct);
                        // No MIN_TEMPERATURE needed as they are parsed together
                    }
                }

                if (msg.getChangeForecasts() != null) {
                    for (TAFChangeForecast changeFct : msg.getChangeForecasts()) {
                        appendToken(retval, FORECAST_CHANGE_INDICATOR, msg, TAF.class, hints, changeFct);
                        appendToken(retval, CHANGE_FORECAST_TIME_GROUP, msg, TAF.class, hints, changeFct);
                        appendToken(retval, SURFACE_WIND, msg, TAF.class, hints, changeFct);
                        appendToken(retval, CAVOK, msg, TAF.class, hints, changeFct);
                        appendToken(retval, HORIZONTAL_VISIBILITY, msg, TAF.class, hints, changeFct);
                        appendToken(retval, NO_SIGNIFICANT_WEATHER, msg, TAF.class, hints, changeFct);
                        if (changeFct.getForecastWeather() != null) {
                            for (Weather weather : changeFct.getForecastWeather()) {
                                appendToken(retval, WEATHER, msg, TAF.class, hints, changeFct, weather);
                            }
                        }
                        clouds = changeFct.getCloud();
                        if (clouds != null) {
                            if (clouds.getVerticalVisibility() != null) {
                                this.appendToken(retval, Identity.CLOUD, msg, TAF.class, hints, "VV", changeFct);
                            } else {
                                this.appendCloudLayers(retval, msg, TAF.class, clouds.getLayers(), hints, changeFct);
                            }
                        }
                    }
                }
                if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
                    appendToken(retval, REMARKS_START, msg, TAF.class, hints);
                    for (String remark : msg.getRemarks()) {
                        this.appendToken(retval, REMARK, msg, TAF.class, hints, remark);
                    }
                }
            }
        } else {
            appendToken(retval, NIL, msg, TAF.class, hints);
        }
        appendToken(retval, END_TOKEN, msg, TAF.class, hints);
        return retval.build();
    }

    private <T extends AviationWeatherMessage> int appendCloudLayers(final LexemeSequenceBuilder builder, final T msg, final Class<T> clz,
            final List<CloudLayer> layers, final ParsingHints hints, final Object... specifier) throws TokenizingException {
        int retval = 0;
        if (layers != null) {
            for (CloudLayer layer : layers) {
            	Object [] params = new Object[specifier.length + 1];
            	params[0] = layer;
            	for (int i = 0; i < specifier.length; i++) {
            		params[i + 1] = specifier[ i ];
            	}
            	
            	
                retval += appendToken(builder, CLOUD, msg, clz, hints, params);
            }
        }
        return retval;
    }

    private <T extends AviationWeatherMessage> int appendToken(final LexemeSequenceBuilder builder, final Identity id, final T msg, final Class<T> clz,
            final ParsingHints hints, final Object... specifier) throws TokenizingException {
        TACTokenReconstructor rec = this.reconstructors.get(id);
        int retval = 0;
        if (rec != null) {
            List<Lexeme> list = rec.getAsLexemes(msg, clz, hints, specifier);
            if (list != null) {
            	for (Lexeme l : list) {
            		builder.append(l);
                    retval++;
                }
            }
        }
        return retval;
    }

}
