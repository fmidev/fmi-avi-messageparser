package fi.fmi.avi.converter.impl;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AUTOMATED;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.COLOR_CODE;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.NO_SIGNIFICANT_CLOUD;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.RECENT_WEATHER;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.REMARK;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.RUNWAY_VISUAL_RANGE;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.SEA_STATE;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.VARIABLE_WIND_DIRECTION;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.WEATHER;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.WIND_SHEAR;

import fi.fmi.avi.converter.ConversionResult;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.Weather;
import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.RunwayState;
import fi.fmi.avi.data.metar.RunwayVisualRange;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.ConversionIssue;
import fi.fmi.avi.converter.ConversionIssue.Type;
import fi.fmi.avi.tac.lexer.SerializingException;
import fi.fmi.avi.tac.lexer.Lexeme;
import fi.fmi.avi.tac.lexer.LexemeSequence;
import fi.fmi.avi.tac.lexer.LexemeSequenceBuilder;
import fi.fmi.avi.tac.lexer.Lexeme.Identity;

/**
 * Created by rinne on 07/06/17.
 */
public class METARTACSerializer extends AbstractTACSerializer<METAR, String> {

    @Override
    public ConversionResult<String> convertMessage(final METAR input, final ConversionHints hints) {
        ConversionResult<String> result = new ConversionResultImpl<String>();
        try {
        	LexemeSequence seq = tokenizeMessage(input, hints);
        	result.setConvertedMessage(seq.getTAC());
        } catch (SerializingException se) {
        	result.addIssue(new ConversionIssue(Type.OTHER, se.getMessage()));
        }
    	return result;
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws SerializingException {
        return tokenizeMessage(msg, null);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException {
        if (!(msg instanceof METAR)) {
            throw new SerializingException("I can only tokenize METARs!");
        }
        METAR input = (METAR) msg;
        LexemeSequenceBuilder retval = this.getLexingFactory().createLexemeSequenceBuilder();
        appendToken(retval, METAR_START, input, METAR.class, hints);
        appendToken(retval, CORRECTION, input, METAR.class, hints);
        appendToken(retval, AERODROME_DESIGNATOR, input, METAR.class, hints);
        appendToken(retval, ISSUE_TIME, input, METAR.class, hints);
        appendToken(retval, AUTOMATED, input, METAR.class, hints);
        appendToken(retval, SURFACE_WIND, input, METAR.class, hints);
        appendToken(retval, VARIABLE_WIND_DIRECTION, input, METAR.class, hints);
        appendToken(retval, CAVOK, input, METAR.class, hints);
        appendToken(retval, HORIZONTAL_VISIBILITY, input, METAR.class, hints);
        if (input.getRunwayVisualRanges() != null) {
            for (RunwayVisualRange range : input.getRunwayVisualRanges()) {
                appendToken(retval, RUNWAY_VISUAL_RANGE, input, METAR.class, hints, range);
            }
        }
        if (input.getPresentWeather() != null) {
            for (Weather weather : input.getPresentWeather()) {
                appendToken(retval, WEATHER, input, METAR.class, hints, weather);
            }
        }
        ObservedClouds obsClouds = input.getClouds();
        if (obsClouds != null) {
            if (obsClouds.getVerticalVisibility() != null) {
                this.appendToken(retval, Lexeme.Identity.CLOUD, input, METAR.class, hints, "VV");
            } else if (obsClouds.isAmountAndHeightUnobservableByAutoSystem()) {
                this.appendToken(retval, Lexeme.Identity.CLOUD, input, METAR.class, hints, "//////");
            } else {
                this.appendCloudLayers(retval, input, METAR.class, obsClouds.getLayers(), hints);
            }
        }
        appendToken(retval, NO_SIGNIFICANT_CLOUD, input, METAR.class, hints);
        appendToken(retval, AIR_DEWPOINT_TEMPERATURE, input, METAR.class, hints);
        appendToken(retval, AIR_PRESSURE_QNH, input, METAR.class, hints);
        if (input.getRecentWeather() != null) {
            for (Weather weather : input.getRecentWeather()) {
                appendToken(retval, RECENT_WEATHER, input, METAR.class, hints, weather);
            }
        }
        appendToken(retval, WIND_SHEAR, input, METAR.class, hints);
        appendToken(retval, SEA_STATE, input, METAR.class, hints);
        if (input.getRunwayStates() != null) {
            for (RunwayState state : input.getRunwayStates()) {
                appendToken(retval, RUNWAY_STATE, input, METAR.class, hints, state);
            }
        }
        appendToken(retval, NO_SIGNIFICANT_WEATHER, input, METAR.class, hints);
        appendToken(retval, COLOR_CODE, input, METAR.class, hints);
        if (input.getTrends() != null) {
            for (TrendForecast trend : input.getTrends()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, input, METAR.class, hints, trend);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, input, METAR.class, hints, trend);
                appendToken(retval, SURFACE_WIND, input, METAR.class, hints, trend);
                appendToken(retval, CAVOK, input, METAR.class, hints, trend);
                appendToken(retval, NO_SIGNIFICANT_WEATHER, input, METAR.class, hints, trend);
                appendToken(retval, HORIZONTAL_VISIBILITY, input, METAR.class, hints, trend);
                if (trend.getForecastWeather() != null) {
                    for (Weather weather : trend.getForecastWeather()) {
                        appendToken(retval, WEATHER, input, METAR.class, hints, trend, weather);
                    }
                }
                appendToken(retval, Identity.NO_SIGNIFICANT_CLOUD, input, METAR.class, hints, trend);
                CloudForecast clouds = trend.getCloud();
                if (clouds != null) {
                    if (clouds.getVerticalVisibility() != null) {
                        this.appendToken(retval, Lexeme.Identity.CLOUD, input, METAR.class, hints, "VV", trend);
                    } else {
                        this.appendCloudLayers(retval, input, METAR.class, clouds.getLayers(), hints, trend);
                    }
                }
                appendToken(retval, COLOR_CODE, input, METAR.class, hints, trend);
            }
        }
        if (input.getRemarks() != null && !input.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, input, METAR.class, hints);
            for (String remark : input.getRemarks()) {
                this.appendToken(retval, REMARK, input, METAR.class, hints, remark);
            }
        }
        appendToken(retval, END_TOKEN, input, METAR.class, hints);
        return retval.build();
    }
}

