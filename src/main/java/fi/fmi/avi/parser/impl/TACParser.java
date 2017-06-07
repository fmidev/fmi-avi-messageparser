package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageLexer;

/**
 * Created by rinne on 07/06/17.
 */
public interface TACParser<T extends AviationWeatherMessage> extends AviMessageSpecificParser<T> {

    void setTACLexer(AviMessageLexer lexer);

}
