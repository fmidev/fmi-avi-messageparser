package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.ParsingResult;

/**
 * A parser capable only parsing only a specific type of input message to a particular
 * {@link AviationWeatherMessage} type.
 *
 * Implementations of this interface are used by the {@link AviMessageParserImpl} to delegate
 * the actual message and input type/format specific parsing.

 * @param <S>
 *           input message type
 * @param <T>
 *          parsed output message type
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public interface AviMessageSpecificParser<S, T extends AviationWeatherMessage> {

    /**
     * Parses a single message.
     *
     * @param input
     *         input message
     * @param hints
     *         parsing hints
     *
     * @return the {@link ParsingResult} with the POJO and the possible parsing issues
     */
    ParsingResult<T> parseMessage(S input, ConversionHints hints);
}
