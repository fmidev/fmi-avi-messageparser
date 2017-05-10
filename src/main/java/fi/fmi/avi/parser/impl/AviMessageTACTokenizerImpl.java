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
        appendAllTokens(retval, SURFACE_WIND, msg, Metar.class, hints);
        appendToken(retval, CAVOK, msg, Metar.class, hints);
        appendToken(retval, HORIZONTAL_VISIBILITY, msg, Metar.class, hints);
        appendAllTokens(retval, RUNWAY_VISUAL_RANGE, msg, Metar.class, hints);
        appendAllTokens(retval, WEATHER, msg, Metar.class, hints);
        ObservedClouds obsClouds = msg.getClouds();
        if (obsClouds != null) {
            if (obsClouds.getVerticalVisibility() != null) {
                this.appendToken(retval, Identity.CLOUD, msg, Metar.class, "VV", hints);
            } else if (obsClouds.isAmountAndHeightUnobservableByAutoSystem()) {
                retval.append(this.factory.createLexeme("//////", Identity.CLOUD));
            } else {
                this.appendCloudLayers(retval, msg, Metar.class, obsClouds.getLayers(), hints);
            }
        }
        appendToken(retval, AIR_DEWPOINT_TEMPERATURE, msg, Metar.class, hints);
        appendToken(retval, AIR_PRESSURE_QNH, msg, Metar.class, hints);
        appendAllTokens(retval, RECENT_WEATHER, msg, Metar.class, hints);
        appendToken(retval, WIND_SHEAR, msg, Metar.class, hints);
        appendToken(retval, SEA_STATE, msg, Metar.class, hints);
        appendAllTokens(retval, RUNWAY_STATE, msg, Metar.class, hints);
        appendToken(retval, NO_SIGNIFICANT_WEATHER, msg, Metar.class, hints);
        if (msg.getTrends() != null) {
            for (TrendForecast trend : msg.getTrends()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, msg, Metar.class, trend, hints);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, msg, Metar.class, trend, hints);
                appendToken(retval, SURFACE_WIND, msg, Metar.class, trend, hints);
                appendToken(retval, CAVOK, msg, Metar.class, trend, hints);
                appendToken(retval, HORIZONTAL_VISIBILITY, msg, Metar.class, trend, hints);
                appendAllTokens(retval, WEATHER, msg, Metar.class, trend, hints);

                CloudForecast clouds = trend.getCloud();
                if (clouds.getVerticalVisibility() != null) {
                	this.appendToken(retval, Identity.CLOUD, msg, Metar.class, "VV", hints);
                } else {
                    this.appendCloudLayers(retval, msg, Metar.class, clouds.getLayers(), hints);
                }
            }
        }
        if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, msg, Metar.class, hints);
            for (String remark : msg.getRemarks()) {
                this.appendToken(retval, REMARK, msg, Metar.class, remark, hints);
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
        appendToken(retval, NIL, msg, TAF.class, hints);

        TAFBaseForecast baseFct = msg.getBaseForecast();
        appendToken(retval, VALID_TIME, msg, TAF.class, baseFct, hints);
        appendToken(retval, CANCELLATION, msg, TAF.class, baseFct, hints);
        appendToken(retval, SURFACE_WIND, msg, TAF.class, baseFct, hints);
        appendToken(retval, CAVOK, msg, TAF.class, baseFct, hints);
        appendToken(retval, HORIZONTAL_VISIBILITY, msg, TAF.class, baseFct, hints);
        appendAllTokens(retval, WEATHER, msg, TAF.class, baseFct, hints);
        appendAllTokens(retval, CLOUD, msg, TAF.class, baseFct, hints);
        appendToken(retval, MIN_TEMPERATURE, msg, TAF.class, baseFct, hints);
        appendToken(retval, MAX_TEMPERATURE, msg, TAF.class, baseFct, hints);

        if (msg.getChangeForecasts() != null) {
            for (TAFChangeForecast changeFct : msg.getChangeForecasts()) {
                appendToken(retval, FORECAST_CHANGE_INDICATOR, msg, TAF.class, changeFct, hints);
                appendToken(retval, CHANGE_FORECAST_TIME_GROUP, msg, TAF.class, changeFct, hints);
                appendToken(retval, SURFACE_WIND, msg, TAF.class, changeFct, hints);
                appendToken(retval, CAVOK, msg, TAF.class, changeFct, hints);
                appendToken(retval, HORIZONTAL_VISIBILITY, msg, TAF.class, changeFct, hints);
                appendAllTokens(retval, WEATHER, msg, TAF.class, changeFct, hints);
                CloudForecast clouds = changeFct.getCloud();
                if (clouds != null) {
                	if (clouds.getVerticalVisibility() != null) {
                    	this.appendToken(retval, Identity.CLOUD, msg, TAF.class, "VV", hints);
                    } else {
                        this.appendCloudLayers(retval, msg, TAF.class, clouds.getLayers(), hints);
                    }
                }
            }
        }
        if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
            appendToken(retval, REMARKS_START, msg, TAF.class, hints);
            for (String remark : msg.getRemarks()) {
                this.appendToken(retval, REMARK, msg, TAF.class, remark, hints);
            }
        }
        appendToken(retval, END_TOKEN, msg, TAF.class, hints);
        return retval.build();
    }

    private <T extends AviationWeatherMessage> void appendCloudLayers(final LexemeSequenceBuilder builder, final T msg, final Class<T> clz, final List<CloudLayer> layers, final ParsingHints hints) throws TokenizingException {
        if (layers != null) {
            for (CloudLayer layer : layers) {
                appendToken(builder, CLOUD, msg, clz, layer, hints);
            }
        }
    }

    private <T extends AviationWeatherMessage> void appendAllTokens(final LexemeSequenceBuilder builder, final Identity id, final T msg, final Class<T> clz, final ParsingHints hints) throws TokenizingException {
        appendAllTokens(builder, id, msg, clz, null);
    }

    private <T extends AviationWeatherMessage> void appendAllTokens(final LexemeSequenceBuilder builder, final Identity id, final T msg, final Class<T> clz, final Object specifier, final ParsingHints hints) throws TokenizingException {
        TACTokenReconstructor rec = this.reconstructors.get(id);
        if (rec != null) {
            List<Lexeme> l = rec.getAllAsLexemes(msg, clz, specifier, hints);
            if (l != null) {
                builder.appendAll(l);
            }
        }
    }
    
    private <T extends AviationWeatherMessage> void appendToken(final LexemeSequenceBuilder builder, final Identity id, final T msg, final Class<T> clz, final ParsingHints hints) throws TokenizingException {
        appendToken(builder, id, msg, clz, null, hints);
    }

    private <T extends AviationWeatherMessage> void appendToken(final LexemeSequenceBuilder builder, final Identity id, final T msg, final Class<T> clz, final Object specifier, final ParsingHints hints) throws TokenizingException {
        TACTokenReconstructor rec = this.reconstructors.get(id);
        if (rec != null) {
            Lexeme l = rec.getAsLexeme(msg, clz, specifier, hints);
            if (l != null) {
                builder.append(l);
            }
        }
    }

}
