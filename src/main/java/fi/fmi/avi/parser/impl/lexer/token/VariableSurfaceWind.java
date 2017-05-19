package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.VARIABLE_WIND_DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MAX_DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MIN_DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class VariableSurfaceWind extends RegexMatchingLexemeVisitor {

    public VariableSurfaceWind(final Priority prio) {
        super("^([0-9]{3})V([0-9]{3})$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        boolean formatOk = true;
        int minDirection, maxDirection;
        minDirection = Integer.parseInt(match.group(1));
        maxDirection = Integer.parseInt(match.group(2));
        if (minDirection < 0 || minDirection >= 360 || maxDirection < 0 || maxDirection >= 360) {
            formatOk = false;
        }
        if (formatOk) {
            token.identify(VARIABLE_WIND_DIRECTION);
            token.setParsedValue(MIN_DIRECTION, minDirection);
            token.setParsedValue(MAX_DIRECTION, maxDirection);
            token.setParsedValue(UNIT, "deg");
        } else {
            token.identify(VARIABLE_WIND_DIRECTION, Lexeme.Status.SYNTAX_ERROR, "Wind directions invalid");
        }
    }
}
