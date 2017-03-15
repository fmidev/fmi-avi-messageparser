package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class Correction extends PrioritizedLexemeVisitor {

    public Correction(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getPrevious() == token.getFirst() && "COR".equalsIgnoreCase(token.getTACToken())) {
            token.identify(CORRECTION);
        }
    }
}
