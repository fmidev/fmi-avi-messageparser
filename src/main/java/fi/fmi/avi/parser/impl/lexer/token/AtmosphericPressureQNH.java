package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class AtmosphericPressureQNH extends RegexMatchingLexemeVisitor {

    public enum PressureMeasurementUnit {
        HECTOPASCAL("Q"), INCHES_OF_MERCURY("A");

        private String code;

        PressureMeasurementUnit(final String code) {
            this.code = code;
        }

        public static PressureMeasurementUnit forCode(final String code) {
            for (PressureMeasurementUnit w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }
    }

    public AtmosphericPressureQNH(final Priority prio) {
        super("^([AQ])([0-9]{4}|////)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        PressureMeasurementUnit unit = PressureMeasurementUnit.forCode(match.group(1));
        Integer value = null;
        if (!"////".equals(match.group(2))) {
            value = Integer.valueOf(match.group(2));
        }
        if (value != null) {
            token.setParsedValue(UNIT, unit);
            token.setParsedValue(VALUE, value);
            token.identify(AIR_PRESSURE_QNH);
        } else {
            token.identify(AIR_PRESSURE_QNH, Lexeme.Status.WARNING, "Missing value for air pressure");
        }
    }
}
