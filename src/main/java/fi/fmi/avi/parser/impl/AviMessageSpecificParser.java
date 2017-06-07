package fi.fmi.avi.parser.impl;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.ParsingResult;

/**
 * Created by rinne on 13/04/17.
 */
public interface AviMessageSpecificParser<T extends AviationWeatherMessage> {

    ParsingResult<T> parseMessage(Object input, ConversionHints hints);
}
