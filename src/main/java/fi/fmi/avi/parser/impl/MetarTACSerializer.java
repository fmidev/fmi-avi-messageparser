package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.AUTOMATED;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.parser.Lexeme.Identity.COLOR_CODE;
import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
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

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.Weather;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.RunwayState;
import fi.fmi.avi.data.metar.RunwayVisualRange;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.Lexeme.Identity;

/**
 * Created by rinne on 07/06/17.
 */
public class MetarTACSerializer extends AbstractTACSerializer<Metar, String> {

    @Override
    public String serializeMessage(final Metar input, final ConversionHints hints) throws SerializingException {
        return tokenizeMessage(input, hints).getTAC();
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws SerializingException {
        return tokenizeMessage(msg, null);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException {
        if (!(msg instanceof Metar)) {
            throw new SerializingException("I can only tokenize METARs!");
        }
        Metar input = (Metar) msg;
        LexemeSequenceBuilder retval = this.getLexingFactory().createLexemeSequenceBuilder();
        appendToken(retval, METAR_START, input, Metar.class, hints);
        appendToken(retval, CORRECTION, input, Metar.class, hints);
        appendToken(retval, AERODROME_DESIGNATOR, input, Metar.class, hints);
        appendToken(retval, ISSUE_TIME, input, Metar.class, hints);
        appendToken(retval, AUTOMATED, input, Metar.class, hints);
        appendToken(retval, SURFACE_WIND, input, Metar.class, hints);
        appendToken(retval, VARIABLE_WIND_DIRECTION, input, Metar.class, hints);
        appendToken(retval, CAVOK, input, Metar.class, hints);
        appendToken(retval, HORIZONTAL_VISIBILITY, input, Metar.class, hints);
        if (input.getRunwayVisualRanges() != null) {
            for (RunwayVisualRange range : input.getRunwayVisualRanges()) {
                appendToken(retval, RUNWAY_VISUAL_RANGE, input, Metar.class, hints, range);
            }
        }
        if (input.getPresentWeather() != null) {
            for (Weather weather : input.getPresentWeather()) {
                appendToken(retval, WEATHER, input, Metar.class, hints, weather);
            }
        }
        ObservedClouds obsClouds = input.getClouds();
        if (obsClouds != null) {
            if (obsClouds.getVerticalVisibility() != null) {
                this.appendToken(retval, Lexeme.Identity.CLOUD, input, Metar.class, hints, "VV");
            } else if (obsClouds.isAmountAndHeightUnobservableByAutoSystem()) {
                this.appendToken(retval, Lexeme.Identity.CLOUD, input, Metar.class, hints, "//////");
            } else {
                this.appendCloudLayers(retval, input, Metar.class, obsClouds.getLayers(), hints);
            }
        }
        appendToken(retval, AIR_DEWPOINT_TEMPERATURE, input, Metar.class, hints);
        appendToken(retval, AIR_PRESSURE_QNH, input, Metar.class, hints);
        if (input.getRecentWeather() != null) {
            for (Weather weather : input.getRecentWeather()) {
                appendToken(retval, RECENT_WEATHER, input, Metar.class, hints, weather);
            }
        }
        appendToken(retval, WIND_SHEAR, input, Metar.class, hints);
        appendToken(retval, SEA_STATE, input, Metar.class, hints);
        if (input.getRunwayStates() != null) {
            for (RunwayState state : input.getRunwayStates()) {
                appendToken(retval, RUNWAY_STATE, input, Metar.class, hints, state);
            }
        }
        
        appendToken(retval, COLOR_CODE, input, Metar.class, hints);
        
        appendToken(retval, NO_SIGNIFICANT_WEATHER, input, Metar.class, hints);
        if (input.getTrends() != null) {
            for (TrendForecast trend : input.getTrends()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, input, Metar.class, hints, trend);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, input, Metar.class, hints, trend);
                appendToken(retval, SURFACE_WIND, input, Metar.class, hints, trend);
                appendToken(retval, CAVOK, input, Metar.class, hints, trend);
                appendToken(retval, NO_SIGNIFICANT_WEATHER, input, Metar.class, hints, trend);
                appendToken(retval, HORIZONTAL_VISIBILITY, input, Metar.class, hints, trend);
                if (trend.getForecastWeather() != null) {
                    for (Weather weather : trend.getForecastWeather()) {
                        appendToken(retval, WEATHER, input, Metar.class, hints, trend, weather);
                    }
                }
                appendToken(retval, Identity.NO_SIGNIFICANT_CLOUD, input, Metar.class, hints, trend);
                CloudForecast clouds = trend.getCloud();
                if (clouds != null) {
                    if (clouds.getVerticalVisibility() != null) {
                        this.appendToken(retval, Lexeme.Identity.CLOUD, input, Metar.class, hints, "VV", trend);
                    } else {
                        this.appendCloudLayers(retval, input, Metar.class, clouds.getLayers(), hints, trend);
                    }
                }
                appendToken(retval, COLOR_CODE, input, Metar.class, hints, trend);
            }
        }
        if (input.getRemarks() != null && !input.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, input, Metar.class, hints);
            for (String remark : input.getRemarks()) {
                this.appendToken(retval, REMARK, input, Metar.class, hints, remark);
            }
        }
        appendToken(retval, END_TOKEN, input, Metar.class, hints);
        return retval.build();
    }
}

