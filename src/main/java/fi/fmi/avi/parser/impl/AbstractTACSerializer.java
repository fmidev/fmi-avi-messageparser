package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudLayer;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.lexer.TACTokenReconstructor;

/**
 * Created by rinne on 07/06/17.
 */
public abstract class AbstractTACSerializer<S extends AviationWeatherMessage, T> implements AviMessageSpecificSerializer<S, T>, AviMessageTACTokenizer {

    private Map<Lexeme.Identity, TACTokenReconstructor> reconstructors = new HashMap<Lexeme.Identity, TACTokenReconstructor>();

    private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

    public void addReconstructor(final Lexeme.Identity id, TACTokenReconstructor reconstructor) {
        reconstructor.setLexingFactory(this.factory);
        this.reconstructors.put(id, reconstructor);
    }

    @Override
    public abstract LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws SerializingException;

    @Override
    public abstract LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException;

    public TACTokenReconstructor getReconstructor(final Lexeme.Identity id) {
        return this.reconstructors.get(id);
    }

    protected <T extends AviationWeatherMessage> int appendCloudLayers(final LexemeSequenceBuilder builder, final T msg, final Class<T> clz,
            final List<CloudLayer> layers, final ConversionHints hints, final Object... specifier) throws SerializingException {
        int retval = 0;
        if (layers != null) {
            for (CloudLayer layer : layers) {
                Object[] params = new Object[specifier.length + 1];
                params[0] = layer;
                for (int i = 0; i < specifier.length; i++) {
                    params[i + 1] = specifier[i];
                }

                retval += appendToken(builder, CLOUD, msg, clz, hints, params);
            }
        }
        return retval;
    }

    protected <T extends AviationWeatherMessage> int appendToken(final LexemeSequenceBuilder builder, final Lexeme.Identity id, final T msg, final Class<T> clz,
            final ConversionHints hints, final Object... specifier) throws SerializingException {
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
