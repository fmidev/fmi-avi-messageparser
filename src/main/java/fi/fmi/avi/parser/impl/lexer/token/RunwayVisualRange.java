package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_VISUAL_RANGE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MAX_VALUE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MIN_VALUE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RELATIONAL_OPERATOR;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RELATIONAL_OPERATOR2;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RUNWAY;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.TENDENCY_OPERATOR;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class RunwayVisibleRange extends RegexMatchingLexemeVisitor {

    public RunwayVisibleRange(final Priority prio) {
        super("^R([0-9]{2}[LRC]?)/([MP])?([0-9]{4})(V([MP])?([0-9]{4}))?([UDN])?(FT)?$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        String runway = match.group(1);
        RecognizingAviMessageTokenLexer.RelationalOperator belowAboveIndicator = RecognizingAviMessageTokenLexer.RelationalOperator.forCode(match.group(2));
        int visibility = Integer.parseInt(match.group(3));
        token.setParsedValue(RUNWAY, runway);
        token.setParsedValue(MIN_VALUE, visibility);
        if (belowAboveIndicator != null) {
            token.setParsedValue(RELATIONAL_OPERATOR, belowAboveIndicator);
        }
        String variablePart = match.group(4);
        if (variablePart != null) {
            belowAboveIndicator = RecognizingAviMessageTokenLexer.RelationalOperator.forCode(match.group(5));
            if (belowAboveIndicator != null) {
                token.setParsedValue(RELATIONAL_OPERATOR2, belowAboveIndicator);
            }
            int variableVis = Integer.parseInt(match.group(6));
            token.setParsedValue(MAX_VALUE, variableVis);
        }
        RecognizingAviMessageTokenLexer.TendencyOperator tendencyIndicator = RecognizingAviMessageTokenLexer.TendencyOperator.forCode(match.group(7));
        if (tendencyIndicator != null) {
            token.setParsedValue(TENDENCY_OPERATOR, tendencyIndicator);
        }
        String unit = match.group(8);
        if (unit != null) {
            token.setParsedValue(UNIT, "ft");
        } else {
            token.setParsedValue(UNIT, "m");
        }
        token.identify(RUNWAY_VISUAL_RANGE);
    }
}
