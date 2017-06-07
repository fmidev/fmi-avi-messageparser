package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AMENDMENT;
import static fi.fmi.avi.parser.Lexeme.Identity.CANCELLATION;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.MAX_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARK;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.Weather;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFAirTemperatureForecast;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.SerializingException;

/**
 * Created by rinne on 07/06/17.
 */
public class TAFTACSerializer extends AbstractTACSerializer<TAF, String> {

    @Override
    public String serializeMessage(final TAF input, final ConversionHints hints) throws SerializingException {
        return tokenizeMessage(input, hints).getTAC();
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws SerializingException {
        return tokenizeMessage(msg, null);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException {
        if (!(msg instanceof TAF)) {
            throw new SerializingException("I can only tokenize TAFs!");
        }
        TAF input = (TAF) msg;
        LexemeSequenceBuilder retval = this.getLexingFactory().createLexemeSequenceBuilder();
        appendToken(retval, TAF_START, input, TAF.class, hints);
        appendToken(retval, AMENDMENT, input, TAF.class, hints);
        appendToken(retval, CORRECTION, input, TAF.class, hints);
        appendToken(retval, AERODROME_DESIGNATOR, input, TAF.class, hints);
        appendToken(retval, ISSUE_TIME, input, TAF.class, hints);

        if (AviationCodeListUser.TAFStatus.MISSING != input.getStatus()) {
            appendToken(retval, VALID_TIME, input, TAF.class, hints);
            appendToken(retval, CANCELLATION, input, TAF.class, hints);
            if (AviationCodeListUser.TAFStatus.CANCELLATION != input.getStatus()) {
                TAFBaseForecast baseFct = input.getBaseForecast();
                if (baseFct == null) {
                    throw new SerializingException("Missing base forecast");
                }
                appendToken(retval, SURFACE_WIND, input, TAF.class, hints, baseFct);
                appendToken(retval, CAVOK, input, TAF.class, hints, baseFct);
                appendToken(retval, HORIZONTAL_VISIBILITY, input, TAF.class, hints, baseFct);
                if (baseFct.getForecastWeather() != null) {
                    for (Weather weather : baseFct.getForecastWeather()) {
                        appendToken(retval, WEATHER, input, TAF.class, hints, baseFct, weather);
                    }
                }
                CloudForecast clouds = baseFct.getCloud();
                if (clouds != null) {
                    if (clouds.getVerticalVisibility() != null) {
                        this.appendToken(retval, Lexeme.Identity.CLOUD, input, TAF.class, hints, "VV", baseFct);
                    } else {
                        this.appendCloudLayers(retval, input, TAF.class, clouds.getLayers(), hints, baseFct);
                    }
                }
                if (baseFct.getTemperatures() != null) {
                    for (TAFAirTemperatureForecast tempFct : baseFct.getTemperatures()) {
                        appendToken(retval, MAX_TEMPERATURE, input, TAF.class, hints, baseFct, tempFct);
                        // No MIN_TEMPERATURE needed as they are produced together
                    }
                }

                if (input.getChangeForecasts() != null) {
                    for (TAFChangeForecast changeFct : input.getChangeForecasts()) {
                        appendToken(retval, FORECAST_CHANGE_INDICATOR, input, TAF.class, hints, changeFct);
                        appendToken(retval, CHANGE_FORECAST_TIME_GROUP, input, TAF.class, hints, changeFct);
                        appendToken(retval, SURFACE_WIND, input, TAF.class, hints, changeFct);
                        appendToken(retval, CAVOK, input, TAF.class, hints, changeFct);
                        appendToken(retval, HORIZONTAL_VISIBILITY, input, TAF.class, hints, changeFct);
                        appendToken(retval, NO_SIGNIFICANT_WEATHER, input, TAF.class, hints, changeFct);
                        if (changeFct.getForecastWeather() != null) {
                            for (Weather weather : changeFct.getForecastWeather()) {
                                appendToken(retval, WEATHER, input, TAF.class, hints, changeFct, weather);
                            }
                        }
                        clouds = changeFct.getCloud();
                        if (clouds != null) {
                            if (clouds.getVerticalVisibility() != null) {
                                this.appendToken(retval, Lexeme.Identity.CLOUD, input, TAF.class, hints, "VV", changeFct);
                            } else {
                                this.appendCloudLayers(retval, input, TAF.class, clouds.getLayers(), hints, changeFct);
                            }
                        }
                    }
                }
                if (input.getRemarks() != null && !input.getRemarks().isEmpty()) {
                    appendToken(retval, REMARKS_START, input, TAF.class, hints);
                    for (String remark : input.getRemarks()) {
                        this.appendToken(retval, REMARK, input, TAF.class, hints, remark);
                    }
                }
            }
        } else {
            appendToken(retval, NIL, input, TAF.class, hints);
        }
        appendToken(retval, END_TOKEN, input, TAF.class, hints);
        return retval.build();
    }
}
