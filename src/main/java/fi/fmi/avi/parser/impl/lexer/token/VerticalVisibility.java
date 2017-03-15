package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.VERTICAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class VerticalVisibility extends RegexMatchingLexemeVisitor {

    public VerticalVisibility(final Priority prio) {
        super("^VV([0-9]{3})$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        int visibility = Integer.parseInt(match.group(1));
        token.setParsedValue(VALUE, visibility);
        token.setParsedValue(UNIT, "hft");
        token.identify(VERTICAL_VISIBILITY);
    }
}
