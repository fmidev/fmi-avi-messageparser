package fi.fmi.avi.parser.impl.lexer;

import java.util.ArrayList;
import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;

public class TACReconstructorAdapter extends FactoryBasedReconstructor {

	@Override
	public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, Object specifier,final ParsingHints hints) throws TokenizingException {
		return null;
	}

	@Override
	public <T extends AviationWeatherMessage> List<Lexeme> getAllAsLexemes(T msg, Class<T> clz, Object specifier, final ParsingHints hints) throws TokenizingException{
		List<Lexeme> retval = null;
		Lexeme l = this.getAsLexeme(msg, clz, specifier, hints);
		if (l != null) {
			retval = new ArrayList<Lexeme>();
			retval.add(l);
		}
		return retval;
	}

}
