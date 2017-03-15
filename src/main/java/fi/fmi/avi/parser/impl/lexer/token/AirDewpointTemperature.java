package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.ArrayList;
import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class AirDewpointTemperature extends RegexMatchingLexemeVisitor {

    public AirDewpointTemperature(final Priority prio) {
        super("^(M)?([0-9]{2}|//)/(M)?([0-9]{2}|//)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        Integer airTemp = null;
        Integer dewPointTemp = null;
        if (!"//".equals(match.group(2))) {
            airTemp = Integer.valueOf(match.group(2));
        }
        if (!"//".equals(match.group(4))) {
            dewPointTemp = Integer.valueOf(match.group(4));
        }
        ArrayList<Integer> values = new ArrayList<Integer>(2);
        boolean missingValues = false;
        if (airTemp != null) {
            if (match.group(1) != null) {
                airTemp = Integer.valueOf(airTemp.intValue() * -1);
            }
            values.add(airTemp);
        } else {
            missingValues = true;
        }
        if (dewPointTemp != null) {
            if (match.group(3) != null) {
                dewPointTemp = Integer.valueOf(dewPointTemp.intValue() * -1);
            }
            values.add(dewPointTemp);
        } else {
            missingValues = true;
        }
        token.setParsedValue(VALUE, values);
        token.setParsedValue(UNIT, "degC");
        if (missingValues) {
            token.identify(AIR_DEWPOINT_TEMPERATURE, Lexeme.Status.WARNING, "Values for air and/or dew point temperature missing");
        } else {
            token.identify(AIR_DEWPOINT_TEMPERATURE);
        }
    }
}
