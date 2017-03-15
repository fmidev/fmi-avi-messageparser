package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.CANCELLATION;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class Cancellation extends PrioritizedLexemeVisitor {

    public Cancellation(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getPrevious() != null && token.getPrevious().getIdentity() == VALID_TIME && "CNL".equalsIgnoreCase(token.getTACToken())) {
            token.identify(CANCELLATION);
        }
    }
}
