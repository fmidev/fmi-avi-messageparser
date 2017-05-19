package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.COLOR_CODE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class ColorCode extends RegexMatchingLexemeVisitor {

    public enum ColorState {
        BLUE("BLU"), WHITE("WHT"), GREEN("GRN"), YELLOW1("YLO1"), YELLOW2("YLO2"), AMBER("AMB"), RED("RED"), BLACK("BLACK");

        private String code;

        ColorState(final String code) {
            this.code = code;
        }

        public static ColorState forCode(final String code) {
            for (ColorState w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }

    }

    public ColorCode(final Priority prio) {
        super("^(BLU|WHT|YLO1|YLO2|AMB|RED)|(BLACK(BLU|WHT|YLO1|YLO2|AMB|RED)?)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        ColorState state;
        if (match.group(2) == null) {
            state = ColorState.forCode(match.group(1));
        } else {
            state = ColorState.BLACK;
        }
        token.identify(COLOR_CODE);
        token.setParsedValue(VALUE, state);
    }
}
