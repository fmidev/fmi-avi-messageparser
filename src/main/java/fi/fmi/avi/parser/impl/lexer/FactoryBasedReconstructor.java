package fi.fmi.avi.parser.impl.lexer;

import java.util.ArrayList;
import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;

/**
 * Created by rinne on 01/03/17.
 */
public abstract class FactoryBasedReconstructor implements TACTokenReconstructor {
	
	protected static <T> T getAs(Object[] specifiers, Class<T> clz) {
		return getAs(specifiers, 0, clz);
	}
	
	@SuppressWarnings("unchecked")
	protected static <T> T getAs(Object[] specifiers, final int index, Class<T> clz) {
		T retval = null;
		if (specifiers != null && specifiers.length > index && clz.isAssignableFrom(specifiers[index].getClass())) {
			retval = (T) specifiers[index];
		}
		return retval;
	}
	
	private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

    @Override
    public <T extends AviationWeatherMessage> List<Lexeme> getAsLexemes(T msg, Class<T> clz, ParsingHints hints, Object... specifier) throws TokenizingException {
    	List<Lexeme> retval = new ArrayList<>();
    	Lexeme lexeme = getAsLexeme(msg, clz, hints, specifier);
    	if (lexeme != null) {
    		retval.add(lexeme);
    	}
    	return retval;
    }
    
    /**
     * Override this unless the class overrides getAsLexemes()
     * 
     * @param msg
     * @param clz
     * @param hints
     * @param specifier
     * @return
     * @throws TokenizingException
     */
    public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ParsingHints hints, Object... specifier) throws TokenizingException {
    	return null;
    }
}
