package fi.fmi.avi.parser.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingException;
import fi.fmi.avi.parser.ParsingHints;

/**
 * Created by rinne on 13/12/16.
 */
public class AviMessageParserImpl implements AviMessageParser {
    private static final Logger LOG = LoggerFactory.getLogger(AviMessageParserImpl.class);

    private Map<Class<? extends AviationWeatherMessage>, AviMessageSpecificParser<? extends AviationWeatherMessage>> parsers = new HashMap<>();

    @Override
    public <T extends AviationWeatherMessage> T parseMessage(final LexemeSequence lexed, final Class<T> type) throws ParsingException {
        return parseMessage(lexed, type, null);
    }

    @Override
    public <T extends AviationWeatherMessage> T parseMessage(final LexemeSequence lexed, final Class<T> type, final ParsingHints hints)
            throws ParsingException {
        for (Class<? extends AviationWeatherMessage> msgClass : parsers.keySet()) {
            if (msgClass.isAssignableFrom(type)) {
                return (T) parsers.get(msgClass).parseMessage(lexed, hints);
            }
        }
        throw new IllegalArgumentException("Unable to parse messsage of type " + type.getCanonicalName());
    }

    public <T extends AviationWeatherMessage> void addMessageSpecificParser(Class<T> messageClass, AviMessageSpecificParser<T> parser) {
        this.parsers.put(messageClass, parser);
    }

    /*
    private static TAF parseTAF(final LexemeSequence lexed, final ParsingHints hints) throws ParsingException {
        TAFImpl retval = new TAFImpl();
        updateBaseForecast(retval, lexed, hints);
        updateChangeForecasts(retval, lexed, hints);
        return retval;
    }

    private static void updateBaseForecast(final TAFImpl taf, final LexemeSequence lexed, final ParsingHints hints) {
        //TODO
    }

    private static void updateChangeForecasts(final TAFImpl taf, final LexemeSequence lexed, final ParsingHints hints) {
        //TODO
    }
    */



}
