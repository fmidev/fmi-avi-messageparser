package fi.fmi.avi.parser.impl.lexer;

import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.SerializingException;

/**
 * Created by rinne on 15/02/17.
 */
public interface TACTokenReconstructor {

    void setLexingFactory(LexingFactory factory);

    /**
     * Returns one or more Lexemes produced by this TACTokenReconstructor using the data from the given message.
     * When more than one alternative Lexemes can be generated based on the data of the given {@code msg},
     * the {@code specifier} parameter is used to specify which Lexeme is intended. If {@code specifier} is not given,
     * the reconstructor must return the first Lexeme (in the TAC token order) it knows how to create.
     *
     * Usually only the one specied Lexeme should be returned. More than one can only be returned if they should
     * immediately follow each other in the TAC message, and are always tightly coupled semantically, such as
     * "PROB30 TEMPO" or "TXM02/3015 TNM10/3103"
     *
     * @param msg
     * @param clz
     * @param hints
     * @param specifier
     * @param <T>
     *
     * @return
     *
     * @throws SerializingException
     */
    <T extends AviationWeatherMessage> List<Lexeme> getAsLexemes(T msg, Class<T> clz, ConversionHints hints, Object... specifier) throws SerializingException;
}
