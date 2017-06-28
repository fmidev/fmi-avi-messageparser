package fi.fmi.avi.parser.impl;

import java.util.HashMap;
import java.util.Map;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.AviMessageSerializer;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.SerializingException;

/**
 * Created by rinne on 07/06/17.
 */
public class AviMessageSerializerImpl implements AviMessageSerializer, AviMessageTACTokenizer {

    private final Map<ConversionSpecification<?, ?>, AviMessageSpecificSerializer<? extends AviationWeatherMessage, ?>> serializers = new HashMap<>();

    @Override
    public <S extends AviationWeatherMessage, T> T serializeMessage(final S input, final ConversionSpecification<T, S> spec) throws SerializingException {
        return serializeMessage(input, spec, null);
    }

    @Override
    public <S extends AviationWeatherMessage, T> T serializeMessage(final S input, final ConversionSpecification<T, S> spec, final ConversionHints hints)
            throws SerializingException {
        for (ConversionSpecification<?, ?> toMatch : serializers.keySet()) {
            if (toMatch.equals(spec)) {
                return ((AviMessageSpecificSerializer<S, T>) serializers.get(spec)).serializeMessage(input, hints);
            }
        }
        throw new IllegalArgumentException("Unable to parse message using specification " + spec);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws SerializingException {
        return this.tokenizeMessage(msg, null);
    }

    @Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException {
        if (msg instanceof METAR) {
            for (ConversionSpecification<?, ?> toMatch : serializers.keySet()) {
                if (toMatch.getInputClass().equals(METAR.class) && toMatch.getOutputClass().equals(String.class)) {
                    AviMessageSpecificSerializer<?, ?> serializer = serializers.get(toMatch);
                    if (serializer instanceof AbstractTACSerializer<?, ?>) {
                        return ((AbstractTACSerializer<?, ?>) serializer).tokenizeMessage(msg, hints);
                    }
                }
            }
        } else if (msg instanceof TAF) {
            for (ConversionSpecification<?, ?> toMatch : serializers.keySet()) {
                if (toMatch.getInputClass().equals(TAF.class) && toMatch.getOutputClass().equals(String.class)) {
                    AviMessageSpecificSerializer<?, ?> serializer = serializers.get(toMatch);
                    if (serializer instanceof AbstractTACSerializer<?, ?>) {
                        return ((AbstractTACSerializer<?, ?>) serializer).tokenizeMessage(msg, hints);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Do not know how to tokenize message of type " + msg.getClass().getCanonicalName());
    }

    public <S extends AviationWeatherMessage, T> void addMessageSpecificSerializer(ConversionSpecification<S, T> spec,
            AviMessageSpecificSerializer<S, T> serializer) {
        this.serializers.put(spec, serializer);
    }
}
