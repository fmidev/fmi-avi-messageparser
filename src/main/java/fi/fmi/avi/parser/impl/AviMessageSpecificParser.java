package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingResult;

/**
 * Created by rinne on 13/04/17.
 */
public interface AviMessageSpecificParser<T extends AviationWeatherMessage> {

    void setTACLexer(AviMessageLexer lexer);

    ParsingResult<T> parseMessage(Object input, ParsingHints hints);
}
