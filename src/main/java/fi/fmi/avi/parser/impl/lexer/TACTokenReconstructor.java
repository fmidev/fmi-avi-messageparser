package fi.fmi.avi.parser.impl.lexer;

import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;

/**
 * Created by rinne on 15/02/17.
 */
public interface TACTokenReconstructor {

    void setLexingFactory(LexingFactory factory);

    <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, Object specifier, ParsingHints hints) throws TokenizingException;
    <T extends AviationWeatherMessage> List<Lexeme> getAllAsLexemes(T msg, Class<T> clz, Object specifier, ParsingHints hints) throws TokenizingException;
}
