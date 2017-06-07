package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_CLOUD;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class NoSignificantCloud extends PrioritizedLexemeVisitor {
    public NoSignificantCloud(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ConversionHints hints) {
        if ("NSC".equalsIgnoreCase(token.getTACToken())) {
            token.identify(NO_SIGNIFICANT_CLOUD);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {
        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ConversionHints hints, Object... specifier)
                throws SerializingException {
            Lexeme retval = null;

            if (TAF.class.isAssignableFrom(clz)) {
                TAFChangeForecast forecast = getAs(specifier, TAFChangeForecast.class);

                if (forecast != null && forecast.isNoSignificantCloud()) {
                    retval = this.createLexeme("NSC", NO_SIGNIFICANT_CLOUD);
                }
            }
            return retval;
        }
    }
}
