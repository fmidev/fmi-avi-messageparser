package fi.fmi.avi.converter.impl;

import fi.fmi.avi.converter.ConversionResult;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.converter.ConversionHints;

/**
 * A parser capable only parsing only a specific type of input message to a particular
 * {@link AviationWeatherMessage} type.
 *
 * Implementations of this interface are used by the {@link AviMessageConverterImpl} to delegate
 * the actual message and input type/format specific parsing.

 * @param <S>
 *           input message type
 * @param <T>
 *          parsed output message type
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public interface AviMessageSpecificConverter<S, T> {

    /**
     * Parses a single message.
     *
     * @param input
     *         input message
     * @param hints
     *         parsing hints
     *
     * @return the {@link ConversionResult} with the POJO and the possible parsing issues
     */
    ConversionResult<T> convertMessage(S input, ConversionHints hints);
}
