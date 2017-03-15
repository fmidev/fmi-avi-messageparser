package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RELATIONAL_OPERATOR;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class MetricHorizontalVisibility extends RegexMatchingLexemeVisitor {

    public MetricHorizontalVisibility(final Priority prio) {
        super("^([0-9]{4})([A-Z]{1,2})?$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        int visibility = Integer.parseInt(match.group(1));
        String direction = match.group(2);
        token.setParsedValue(UNIT, "m");
        if (direction != null) {
            token.setParsedValue(DIRECTION, direction);
        }
        if (visibility == 9999) {
            token.setParsedValue(VALUE, 10000);
            token.setParsedValue(RELATIONAL_OPERATOR, RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN);
        } else if (visibility == 0) {
            token.setParsedValue(VALUE, 50);
            token.setParsedValue(RELATIONAL_OPERATOR, RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN);
        }
        token.identify(HORIZONTAL_VISIBILITY);
    }
}
