package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class NoSignificantWeather extends PrioritizedLexemeVisitor {
    public NoSignificantWeather(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if ("NOSIG".equalsIgnoreCase(token.getTACToken()) || "NSW".equalsIgnoreCase(token.getTACToken()) || "NSC".equalsIgnoreCase(token.getTACToken())) {
            token.identify(NO_SIGNIFICANT_WEATHER);
        }
    }
    
    
    public static class Reconstructor extends FactoryBasedReconstructor {
    	@Override
    	public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ParsingHints hints,
    			Object... specifier) throws TokenizingException {
			// Produce NSW's only when long format is requested
			if (hints.get(ParsingHints.KEY_VALIDTIME_FORMAT) != ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_LONG) {
				return null;
			}

			Lexeme retval = null;
			
			if (TAF.class.isAssignableFrom(clz)) {
				TAFChangeForecast forecast = getAs(specifier, TAFChangeForecast.class);
				
				if (forecast != null) {
					if (forecast.getForecastWeather() == null || forecast.getForecastWeather().isEmpty()) {
						retval = this.getLexingFactory().createLexeme("NSW", NO_SIGNIFICANT_WEATHER);
					}
				}
			}
			
			
			return retval;
    	}
    }
}
