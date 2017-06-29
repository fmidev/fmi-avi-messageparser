package fi.fmi.avi.tac.lexer.impl.token;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.NO_SIGNIFICANT_CLOUD;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.tac.lexer.SerializingException;
import fi.fmi.avi.tac.lexer.Lexeme;
import fi.fmi.avi.tac.lexer.impl.FactoryBasedReconstructor;
import fi.fmi.avi.tac.lexer.impl.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class NoSignificantCloud extends RegexMatchingLexemeVisitor {
    public NoSignificantCloud(final Priority prio) {
        super("^NSC$", prio);
    }

    @Override
    public void visitIfMatched(Lexeme token, Matcher match, ConversionHints hints) {
    	token.identify(NO_SIGNIFICANT_CLOUD);
    }

    public static class Reconstructor extends FactoryBasedReconstructor {
        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ConversionHints hints, Object... specifier)
                throws SerializingException {
            Lexeme retval = null;

            boolean nsc = false;
            
            if (TAF.class.isAssignableFrom(clz)) {
                TAFChangeForecast forecast = getAs(specifier, TAFChangeForecast.class);

                if (forecast != null && forecast.isNoSignificantCloud()) {
                    nsc = true;
                }
            }
            
            if (METAR.class.isAssignableFrom(clz)) {
            	METAR metar = (METAR)msg;
            	TrendForecast forecast = getAs(specifier, TrendForecast.class);
            	if (forecast != null) {
            		if (forecast.isNoSignificantCloud()) {
            			nsc = true;
            		}
            	} else if (metar.isNoSignificantCloud()) {
            		nsc = true;
            	}
            }
            
            if (nsc) {
            	retval = this.createLexeme("NSC", NO_SIGNIFICANT_CLOUD);
            }
            
            return retval;
        }
    }
}
