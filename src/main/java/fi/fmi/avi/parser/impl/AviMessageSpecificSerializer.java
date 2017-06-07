package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.SerializingException;

/**
 * Created by rinne on 07/06/17.
 */
public interface AviMessageSpecificSerializer<S extends AviationWeatherMessage, T> {

    T serializeMessage(S input, ConversionHints hints) throws SerializingException;
}
