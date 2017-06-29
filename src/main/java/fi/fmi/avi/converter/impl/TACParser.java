package fi.fmi.avi.converter.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.tac.lexer.AviMessageLexer;

/**
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public interface TACParser<S, T extends AviationWeatherMessage> extends AviMessageSpecificConverter<S, T> {

    void setTACLexer(AviMessageLexer lexer);

}
