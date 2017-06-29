package fi.fmi.avi.tac.lexer.impl.token;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.tac.lexer.Lexeme.ParsedValueName.RELATIONAL_OPERATOR;
import static fi.fmi.avi.tac.lexer.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.tac.lexer.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.tac.lexer.Lexeme;
import fi.fmi.avi.tac.lexer.impl.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.tac.lexer.impl.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class FractionalHorizontalVisibility extends RegexMatchingLexemeVisitor {

    public FractionalHorizontalVisibility(final Priority prio) {
        super("^([PM])?([0-9]*\\s)?([0-9]*/[0-9]*)([A-Z]{2})$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints) {
    	token.identify(HORIZONTAL_VISIBILITY);
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
                token.setParsedValue(VALUE, Double.valueOf(wholePart + (double) fractionNumenator / (double) fractionDenumenator));
            }
        } else if (wholePart > -1) {
            token.setParsedValue(VALUE, Double.valueOf(wholePart));
        }
        String unit = match.group(4).toLowerCase();
        token.setParsedValue(UNIT, unit.toLowerCase());
        
    }
}
