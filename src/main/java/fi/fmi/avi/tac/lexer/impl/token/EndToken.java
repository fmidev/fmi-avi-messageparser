package fi.fmi.avi.tac.lexer.impl.token;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.END_TOKEN;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.tac.lexer.Lexeme;
import fi.fmi.avi.tac.lexer.impl.FactoryBasedReconstructor;
import fi.fmi.avi.tac.lexer.impl.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class EndToken extends PrioritizedLexemeVisitor {
    public EndToken(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ConversionHints hints) {
        if (token.getNext() == null && "=".equalsIgnoreCase(token.getTACToken())) {
            token.identify(END_TOKEN);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final ConversionHints hints, final Object... specifier) {
            return this.createLexeme("=", Lexeme.Identity.END_TOKEN);
        }
    }

}
