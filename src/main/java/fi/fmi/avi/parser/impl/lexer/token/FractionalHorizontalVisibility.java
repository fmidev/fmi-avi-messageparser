package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
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
public class FractionalHorizontalVisibility extends RegexMatchingLexemeVisitor {

    public FractionalHorizontalVisibility(final Priority prio) {
        super("^([PM])?([0-9]*\\s)?([0-9]*/[0-9]*)([A-Z]{2})$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        RecognizingAviMessageTokenLexer.RelationalOperator modifier = RecognizingAviMessageTokenLexer.RelationalOperator.forCode(match.group(1));
        if (modifier != null) {
            token.setParsedValue(RELATIONAL_OPERATOR, modifier);
        }
        String s = match.group(2);
        int wholePart = -1;
        if (s != null) {
            wholePart = Integer.parseInt(s.trim());
        }
        s = match.group(3);
        if (s != null) {
            if (wholePart == -1) {
                wholePart = 0;
            }
            int fractionNumenator = Integer.parseInt(s.substring(0, s.indexOf('/')));
            int fractionDenumenator = Integer.parseInt(s.substring(s.indexOf('/') + 1));
            if (fractionDenumenator != 0) {
                token.setParsedValue(VALUE, new Double(wholePart + (double) fractionNumenator / (double) fractionDenumenator));
            }
        } else if (wholePart > -1) {
            token.setParsedValue(VALUE, wholePart);
        }
        String unit = match.group(4).toLowerCase();
        token.setParsedValue(UNIT, unit.toLowerCase());
        token.identify(HORIZONTAL_VISIBILITY);
    }
}
