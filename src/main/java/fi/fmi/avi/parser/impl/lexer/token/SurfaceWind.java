package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MAX_VALUE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MEAN_VALUE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class SurfaceWind extends RegexMatchingLexemeVisitor {

    public enum WindDirection {
        VARIABLE("VRB");

        private String code;

        WindDirection(final String code) {
            this.code = code;
        }

        public static WindDirection forCode(final String code) {
            for (WindDirection w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }
    }

    public SurfaceWind(final Priority prio) {
        super("^(VRB|[0-9]{3})([0-9]{2})(G[0-9]{2})?(KT|MPS)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        boolean formatOk = true;
        int direction = -1, mean, gustValue = -1;
        String unit;
        if (!"VRB".equals(match.group(1))) {
            direction = Integer.parseInt(match.group(1));
        }
        mean = Integer.parseInt(match.group(2));
        String gust = match.group(3);
        if (gust != null && 'G' == gust.charAt(0)) {
            try {
                gustValue = Integer.parseInt(gust.substring(1));
                if (gustValue < 0) {
                    formatOk = false;
                }
            } catch (NumberFormatException nfe) {
                formatOk = false;
            }
        }
        unit = match.group(4);
        if (direction >= 360 || mean < 0 || unit == null) {
            formatOk = false;
        }

        if (formatOk) {
            if (direction == -1) {
                token.setParsedValue(DIRECTION, WindDirection.VARIABLE);
            } else {
                token.setParsedValue(DIRECTION, Integer.valueOf(direction));
            }
            token.setParsedValue(MEAN_VALUE, Integer.valueOf(mean));
            if (gustValue > -1) {
                token.setParsedValue(MAX_VALUE, Integer.valueOf(gustValue));
            }
            token.setParsedValue(UNIT, unit);
            token.identify(SURFACE_WIND);
        } else {
            token.identify(SURFACE_WIND, Lexeme.Status.SYNTAX_ERROR, "Wind direction or speed values invalid");
        }
    }

}
