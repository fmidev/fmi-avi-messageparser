package fi.fmi.avi.parser.impl;

import java.util.HashMap;
import java.util.Map;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingResult;

/**
 * Created by rinne on 13/12/16.
 */
public class AviMessageParserImpl implements AviMessageParser {
    private final Map<ParserSpecification, AviMessageSpecificParser<? extends AviationWeatherMessage>> parsers = new HashMap<>();

    @Override
    public <S, T extends AviationWeatherMessage> ParsingResult<T> parseMessage(final S input, final Class<S> inputClz, final Class<T> outputClz) {
        return parseMessage(input, inputClz, outputClz, null);
    }

    @Override
    public <S, T extends AviationWeatherMessage> ParsingResult<T> parseMessage(final S input, final Class<S> inputClz, final Class<T> outputClz,
            final ParsingHints hints) {
        for (ParserSpecification spec : parsers.keySet()) {
            if (spec.getInput().isAssignableFrom(inputClz) && spec.getOutput().isAssignableFrom(outputClz)) {
                return (ParsingResult<T>) parsers.get(spec).parseMessage(input, hints);
            }
        }
        throw new IllegalArgumentException("Unable to parse message of type " + inputClz.getCanonicalName() + " to " + outputClz.getCanonicalName());
    }

    public <T extends AviationWeatherMessage, S> void addMessageSpecificParser(Class<S> inputClz, Class<T> outputClz, AviMessageSpecificParser<T> parser) {
        this.parsers.put(new ParserSpecification(inputClz, outputClz), parser);
    }

    static class ParserSpecification {
        private Class<?> input;
        private Class<? extends AviationWeatherMessage> output;

        public ParserSpecification(final Class input, final Class<? extends AviationWeatherMessage> output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final ParserSpecification that = (ParserSpecification) o;

            if (input != null ? !input.equals(that.input) : that.input != null) {
                return false;
            }
            return output != null ? output.equals(that.output) : that.output == null;
        }

        @Override
        public int hashCode() {
            int result = input != null ? input.hashCode() : 0;
            result = 31 * result + (output != null ? output.hashCode() : 0);
            return result;
        }

        public Class getInput() {
            return input;
        }

        public Class<? extends AviationWeatherMessage> getOutput() {
            return output;
        }
    }

}
