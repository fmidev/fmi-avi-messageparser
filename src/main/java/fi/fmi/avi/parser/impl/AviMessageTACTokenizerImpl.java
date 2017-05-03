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
import static fi.fmi.avi.parser.Lexeme.Identity.MIN_TEMPERATURE;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.CloudLayer;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.ParsingHints;
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
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) {
        return this.tokenizeMessage(msg, null);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ParsingHints hints) {
        if (msg instanceof Metar) {
            return tokenizeMetar((Metar) msg, hints);
        } else if (msg instanceof TAF) {
            return tokenizeTAF((TAF) msg, hints);
        } else {
            throw new IllegalArgumentException("Do not know how to tokenize message of type " + msg.getClass().getCanonicalName());
        }
    }

    private LexemeSequence tokenizeMetar(final Metar msg, final ParsingHints hints) {
        LexemeSequenceBuilder retval = this.factory.createLexemeSequenceBuilder();
        appendToken(retval, METAR_START, msg, Metar.class);
        appendToken(retval, CORRECTION, msg, Metar.class);
        appendToken(retval, AERODROME_DESIGNATOR, msg, Metar.class);
        appendToken(retval, ISSUE_TIME, msg, Metar.class);
        appendToken(retval, AUTOMATED, msg, Metar.class);
        appendToken(retval, SURFACE_WIND, msg, Metar.class);
        appendToken(retval, CAVOK, msg, Metar.class);
        appendToken(retval, HORIZONTAL_VISIBILITY, msg, Metar.class);
        appendToken(retval, RUNWAY_VISUAL_RANGE, msg, Metar.class);
        appendToken(retval, WEATHER, msg, Metar.class);
        ObservedClouds obsClouds = msg.getClouds();
        if (obsClouds != null) {
            if (obsClouds.getVerticalVisibility() != null) {
                this.appendToken(retval, Identity.CLOUD, msg, Metar.class, "VV");
            } else if (obsClouds.isAmountAndHeightUnobservableByAutoSystem()) {
                retval.append(this.factory.createLexeme("//////", Identity.CLOUD));
            } else {
                this.appendCloudLayers(retval, msg, Metar.class, obsClouds.getLayers());
            }
        }
        appendToken(retval, AIR_DEWPOINT_TEMPERATURE, msg, Metar.class);
        appendToken(retval, AIR_PRESSURE_QNH, msg, Metar.class);
        appendToken(retval, RECENT_WEATHER, msg, Metar.class);
        appendToken(retval, WIND_SHEAR, msg, Metar.class);
        appendToken(retval, SEA_STATE, msg, Metar.class);
        appendToken(retval, RUNWAY_STATE, msg, Metar.class);
        appendToken(retval, NO_SIGNIFICANT_WEATHER, msg, Metar.class);
        if (msg.getTrends() != null) {
            for (TrendForecast trend : msg.getTrends()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, msg, Metar.class, trend);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, msg, Metar.class, trend);
                appendToken(retval, SURFACE_WIND, msg, Metar.class, trend);
                appendToken(retval, CAVOK, msg, Metar.class, trend);
                appendToken(retval, HORIZONTAL_VISIBILITY, msg, Metar.class, trend);
                appendToken(retval, WEATHER, msg, Metar.class, trend);

                CloudForecast clouds = trend.getCloud();
                if (clouds.getVerticalVisibility() != null) {
                	this.appendToken(retval, Identity.CLOUD, msg, Metar.class, "VV");
                } else {
                    this.appendCloudLayers(retval, msg, Metar.class, clouds.getLayers());
                }
            }
        }
        if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, msg, Metar.class);
            for (String remark : msg.getRemarks()) {
                this.appendToken(retval, REMARK, msg, Metar.class, remark);
            }
        }
        appendToken(retval, END_TOKEN, msg, Metar.class);
        return retval.build();

    }

    private LexemeSequence tokenizeTAF(final TAF msg, final ParsingHints hints) {
        LexemeSequenceBuilder retval = this.factory.createLexemeSequenceBuilder();

        appendToken(retval, TAF_START, msg, TAF.class);
        appendToken(retval, AMENDMENT, msg, TAF.class);
        appendToken(retval, CORRECTION, msg, TAF.class);
        appendToken(retval, AERODROME_DESIGNATOR, msg, TAF.class);
        appendToken(retval, ISSUE_TIME, msg, TAF.class);
        appendToken(retval, NIL, msg, TAF.class);

        TAFBaseForecast baseFct = msg.getBaseForecast();
        appendToken(retval, VALID_TIME, msg, TAF.class, baseFct);
        appendToken(retval, CANCELLATION, msg, TAF.class, baseFct);
        appendToken(retval, SURFACE_WIND, msg, TAF.class, baseFct);
        appendToken(retval, CAVOK, msg, TAF.class, baseFct);
        appendToken(retval, HORIZONTAL_VISIBILITY, msg, TAF.class, baseFct);
        appendToken(retval, WEATHER, msg, TAF.class, baseFct);
        appendToken(retval, CLOUD, msg, TAF.class, baseFct);
        appendToken(retval, MIN_TEMPERATURE, msg, TAF.class, baseFct);
        appendToken(retval, MAX_TEMPERATURE, msg, TAF.class, baseFct);

        if (msg.getChangeForecasts() != null) {
            for (TAFChangeForecast changeFct : msg.getChangeForecasts()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, msg, TAF.class, changeFct);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, msg, TAF.class, changeFct);
                appendToken(retval, SURFACE_WIND, msg, TAF.class, changeFct);
                appendToken(retval, CAVOK, msg, TAF.class, changeFct);
                appendToken(retval, HORIZONTAL_VISIBILITY, msg, TAF.class, changeFct);
                appendToken(retval, WEATHER, msg, TAF.class, changeFct);
                appendToken(retval, CLOUD, msg, TAF.class, changeFct);
            }
        }
        if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, msg, TAF.class);
            for (String remark : msg.getRemarks()) {
                this.appendToken(retval, REMARK, msg, TAF.class, remark);
            }
        }
        appendToken(retval, END_TOKEN, msg, TAF.class);
        return retval.build();
    }

    private <T extends AviationWeatherMessage> void appendCloudLayers(LexemeSequenceBuilder builder, T msg, Class<T> clz, List<CloudLayer> layers) {
        if (layers != null) {
            for (CloudLayer layer : layers) {
                appendToken(builder, CLOUD, msg, clz, layer);
            }
        }
    }

    private <T extends AviationWeatherMessage> void appendToken(LexemeSequenceBuilder builder, Identity id, T msg, Class<T> clz) {
        appendToken(builder, id, msg, clz, null);
    }

    private <T extends AviationWeatherMessage> void appendToken(LexemeSequenceBuilder builder, Identity id, T msg, Class<T> clz, Object specifier) {
        TACTokenReconstructor rec = this.reconstructors.get(id);
        if (rec != null) {
            Lexeme l = rec.getAsLexeme(msg, clz, specifier);
            if (l != null) {
                builder.append(l);
            }
        }
    }

}
