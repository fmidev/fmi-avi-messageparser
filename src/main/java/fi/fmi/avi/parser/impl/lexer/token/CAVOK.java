package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class CAVOK extends PrioritizedLexemeVisitor {
    public CAVOK(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if ("CAVOK".equalsIgnoreCase(token.getTACToken())) {
            token.identify(CAVOK);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final Object specifier) {
            Lexeme retval = null;
            if (Metar.class.isAssignableFrom(clz)) {
                Metar m = (Metar) msg;
                if (specifier == null) {
                    if (m.isCeilingAndVisibilityOk()) {
                        retval = this.getLexingFactory().createLexeme("CAVOK", CAVOK);
                    }
                } else if (specifier instanceof TAFChangeForecast) {
                    TAFChangeForecast c = (TAFChangeForecast) specifier;
                    if (c.isCeilingAndVisibilityOk()) {
                        retval = this.getLexingFactory().createLexeme("CAVOK", CAVOK);
                    }
                }
            } else if (TAF.class.isAssignableFrom(clz)) {

            }
            return retval;
        }
    }
}
