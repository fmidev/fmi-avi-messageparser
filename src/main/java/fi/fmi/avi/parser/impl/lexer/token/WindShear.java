package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.WIND_SHEAR;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RUNWAY;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class WindShear extends RegexMatchingLexemeVisitor {

    public WindShear(final Priority prio) {
        super("^WS\\s(ALL\\s)?RWY([0-9]{2}[LRC]?)?$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        if (match.group(1) != null) {
        	token.identify(WIND_SHEAR);
            token.setParsedValue(RUNWAY, "ALL");
        } else if (match.group(2) != null) {
        	token.identify(WIND_SHEAR);
            token.setParsedValue(RUNWAY, match.group(2));
        } else {
            token.identify(WIND_SHEAR, Lexeme.Status.SYNTAX_ERROR, "Could not understand runway code");
        }
    }
}
