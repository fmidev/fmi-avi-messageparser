package fi.fmi.avi.parser.impl;

import java.util.HashMap;
import java.util.Map;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.ParserSpecification;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingResult;

/**
 * Created by rinne on 13/12/16.
 */
public class AviMessageParserImpl implements AviMessageParser {
    private final Map<ParserSpecification<?, ?>, AviMessageSpecificParser<? extends AviationWeatherMessage>> parsers = new HashMap<>();

    @Override
    public <S, T extends AviationWeatherMessage> ParsingResult<T> parseMessage(final S input, final ParserSpecification<S, T> spec) {
        return parseMessage(input, spec, null);
    }

    @Override
    public <S, T extends AviationWeatherMessage> ParsingResult<T> parseMessage(final S input, final ParserSpecification<S, T> spec, final ParsingHints hints) {
        for (ParserSpecification<?, ?> toMatch : parsers.keySet()) {
            if (toMatch.equals(spec)) {
                return (ParsingResult<T>) parsers.get(spec).parseMessage(input, hints);
            }
        }
        throw new IllegalArgumentException("Unable to parse message using specification " + spec);
    }

    public <T extends AviationWeatherMessage, S> void addMessageSpecificParser(ParserSpecification<S, T> spec, AviMessageSpecificParser<T> parser) {
        this.parsers.put(spec, parser);
    }

}
