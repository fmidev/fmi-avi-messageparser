package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.SEA_STATE;

import java.util.ArrayList;
import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class SeaState extends RegexMatchingLexemeVisitor {

    public enum SeaSurfaceState {
        CALM_GLASSY('0'),
        CALM_RIPPLED('1'),
        SMOOTH_WAVELETS('2'),
        SLIGHT('3'),
        MODERATE('4'),
        ROUGH('5'),
        VERY_ROUGH('6'),
        HIGH('7'),
        VERY_HIGH('8'),
        PHENOMENAL('9'),
        MISSING('/');

        private char code;

        SeaSurfaceState(final char code) {
            this.code = code;
        }

        public static SeaSurfaceState forCode(final char code) {
            for (SeaSurfaceState w : values()) {
                if (w.code == code) {
                    return w;
                }
            }
            return null;
        }
    }

    public SeaState(final Priority prio) {
        super("^W(M?)([0-9]{2}|//)/S([0-9]|/)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        Integer seaSurfaceTemperature = null;
        if (!"//".equals(match.group(2))) {
            seaSurfaceTemperature = Integer.valueOf(match.group(2));
        }
        if (seaSurfaceTemperature != null && match.group(1) != null) {
            seaSurfaceTemperature = Integer.valueOf(seaSurfaceTemperature.intValue() * -1);
        }
        SeaSurfaceState state = SeaSurfaceState.forCode(match.group(3).charAt(0));
        ArrayList<Object> values = new ArrayList<Object>(2);
        values.add(seaSurfaceTemperature);
        values.add(state);
        token.setParsedValue(Lexeme.ParsedValueName.VALUE, values);
        token.identify(SEA_STATE);
    }
}
