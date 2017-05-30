package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class EndToken extends PrioritizedLexemeVisitor {
    public EndToken(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getNext() == null && "=".equalsIgnoreCase(token.getTACToken())) {
            token.identify(END_TOKEN);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final ParsingHints hints, final Object... specifier) {
            return this.createLexeme("=", Lexeme.Identity.END_TOKEN);
        }
    }

}
