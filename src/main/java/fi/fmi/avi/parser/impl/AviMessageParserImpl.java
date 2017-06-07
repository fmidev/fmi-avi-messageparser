package fi.fmi.avi.parser.impl;

import java.util.HashMap;
import java.util.Map;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.ParsingResult;

/**
 * The main parser implementation class. Turns aviation weather messages encoded in different formats
 * into {@link AviationWeatherMessage}s.
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public class AviMessageParserImpl implements AviMessageParser {

    private final Map<ConversionSpecification<?, ?>, AviMessageSpecificParser<?, ? extends AviationWeatherMessage>> parsers = new HashMap<>();

    /**
     * Parses the input message into a Java POJO. Delegates the actual parsing work to the first
     * {@link AviMessageSpecificParser} implementation with the matching {@link ConversionSpecification}.
     *
     * @param input
     * @param spec
     *         {@link ConversionSpecification} to use
     * @param <S>
     *         the type if the input message
     * @param <T>
     *         the type of the POJO to return
     *
     * @return the {@link ParsingResult} with the POJO and the possible parsing issues
     *
     * @see #addMessageSpecificParser(ConversionSpecification, AviMessageSpecificParser)
     */
    @Override
    public <S, T extends AviationWeatherMessage> ParsingResult<T> parseMessage(final S input, final ConversionSpecification<S, T> spec) {
        return parseMessage(input, spec, null);
    }

    /**
     * Parses the input message into a Java POJO. Delegates the actual parsing work to the first
     * {@link AviMessageSpecificParser} implementation with the matching {@link ConversionSpecification}
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
     * @return the {@link ParsingResult} with the POJO and the possible parsing issues
     *
     * @see #addMessageSpecificParser(ConversionSpecification, AviMessageSpecificParser)
     */
    @Override
    public <S, T extends AviationWeatherMessage> ParsingResult<T> parseMessage(final S input, final ConversionSpecification<S, T> spec,
            final ConversionHints hints) {
        for (ConversionSpecification<?, ?> toMatch : parsers.keySet()) {
            if (toMatch.equals(spec)) {
                return ((AviMessageSpecificParser<S, T>) parsers.get(spec)).parseMessage(input, hints);
            }
        }
        throw new IllegalArgumentException("Unable to parse message using specification " + spec);
    }

    /**
     * Adds parsing capability for the given {@link ConversionSpecification} to this parser instance.
     *
     * @param spec
     *         the parsing capability specification including input and output formats
     * @param parser
     *         the parser implementation
     * @param <S>
     *         the type if the input message
     * @param <T>
     *         the type of the POJO to return
     */
    public <S, T extends AviationWeatherMessage> void addMessageSpecificParser(ConversionSpecification<S, T> spec, AviMessageSpecificParser<S, T> parser) {
        this.parsers.put(spec, parser);
    }

}
