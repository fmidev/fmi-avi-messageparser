package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AUTOMATED;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class AutoMetar extends PrioritizedLexemeVisitor {
    public AutoMetar(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if ("AUTO".equalsIgnoreCase(token.getTACToken())) {
            token.identify(AUTOMATED);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final ParsingHints hints, final Object... specifier) {
            if (Metar.class.isAssignableFrom(clz)) {
                return this.createLexeme("AUTO", Lexeme.Identity.AUTOMATED);
            } else {
                return null;
            }

        }
    }
}
