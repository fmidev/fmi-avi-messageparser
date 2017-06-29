package fi.fmi.avi.converter.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.tac.lexer.SerializingException;

/**
 * Created by rinne on 07/06/17.
 */
public interface AviMessageSpecificSerializer<S extends AviationWeatherMessage, T> {

    T serializeMessage(S input, ConversionHints hints) throws SerializingException;
}
