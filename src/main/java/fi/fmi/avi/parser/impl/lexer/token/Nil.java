package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class Nil extends PrioritizedLexemeVisitor {

    public Nil(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getPrevious() != null && token.getPrevious().getIdentity() == ISSUE_TIME && "NIL".equalsIgnoreCase(token.getTACToken())) {
            token.identify(NIL);
        }
    }
}
