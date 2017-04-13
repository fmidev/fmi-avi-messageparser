package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingException;
import fi.fmi.avi.parser.ParsingHints;

/**
 * Created by rinne on 13/04/17.
 */
public interface AviMessageSpecificParser<T extends AviationWeatherMessage> {

    T parseMessage(final LexemeSequence lexed, final ParsingHints hints) throws ParsingException;
}
