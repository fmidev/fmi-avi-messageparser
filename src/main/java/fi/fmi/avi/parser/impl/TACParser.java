package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageLexer;

/**
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public interface TACParser<S, T extends AviationWeatherMessage> extends AviMessageSpecificParser<S, T> {

    void setTACLexer(AviMessageLexer lexer);

}
