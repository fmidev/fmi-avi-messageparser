package fi.fmi.avi.converter.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fi.fmi.avi.converter.AviMessageConverter;
import fi.fmi.avi.converter.ConversionResult;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.ConversionSpecification;

/**
 * The main parser implementation class. Turns aviation weather messages encoded in different formats
 * into {@link AviationWeatherMessage}s.
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public class AviMessageConverterImpl implements AviMessageConverter {

    private final Map<ConversionSpecification<?, ?>, AviMessageSpecificConverter<?, ?>> parsers = new HashMap<>();

    /**
     * Parses the input message into a Java POJO. Delegates the actual parsing work to the first
     * {@link AviMessageSpecificConverter} implementation with the matching {@link ConversionSpecification}.
     *
     * @param input
     *          the input message
     * @param spec
     *         {@link ConversionSpecification} to use
     * @param <S>
     *         the type if the input message
     * @param <T>
     *         the type of the POJO to return
     *
     * @return the {@link ConversionResult} with the POJO and the possible parsing issues
     *
     * @see #addMessageSpecificConverter(ConversionSpecification, AviMessageSpecificConverter)
     */
    @Override
    public <S, T> ConversionResult<T> convertMessage(final S input, final ConversionSpecification<S, T> spec) {
        return convertMessage(input, spec, null);
    }

    /**
     * Parses the input message into a Java POJO. Delegates the actual parsing work to the first
     * {@link AviMessageSpecificConverter} implementation with the matching {@link ConversionSpecification}
     * using the given {@link ConversionHints}.
     *
     * @param input
     *         the input message
     * @param spec
     *         {@link ConversionSpecification} to use
     * @param hints
     *         the parsing hints to guide the parsing implementation
     * @param <S>
     *         the type if the input message
     * @param <T>
     *         the type of the POJO to return
     *
     * @return the {@link ConversionResult} with the POJO and the possible parsing issues
     *
     * @see #addMessageSpecificConverter(ConversionSpecification, AviMessageSpecificConverter)
     */
    @Override
    public <S, T> ConversionResult<T> convertMessage(final S input, final ConversionSpecification<S, T> spec,
            final ConversionHints hints) {
        for (ConversionSpecification<?, ?> toMatch : parsers.keySet()) {
            if (toMatch.equals(spec)) {
                return ((AviMessageSpecificConverter<S, T>) parsers.get(spec)).convertMessage(input, hints);
            }
        }
        throw new IllegalArgumentException("Unable to parse message using specification " + spec);
    }

    /**
     * Adds converting capability for the given {@link ConversionSpecification} to this converter instance.
     *
     * @param spec
     *         the parsing capability specification including input and output formats
     * @param converter
     *         the converter implementation
     * @param <S>
     *         the type if the input message
     * @param <T>
     *         the type of the message to return
     */
    public <S, T> void addMessageSpecificConverter(ConversionSpecification<S, T> spec, AviMessageSpecificConverter<S, T> converter) {
        this.parsers.put(spec, converter);
    }
    
    @Override
    public Set<ConversionSpecification<?,?>> getSupportedSpecifications() {
    	return this.parsers.keySet();
    }

}
