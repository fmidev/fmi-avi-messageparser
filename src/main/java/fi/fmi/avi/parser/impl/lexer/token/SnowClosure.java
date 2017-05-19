package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.HashMap;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;
import fi.fmi.avi.parser.impl.lexer.token.RunwayState.RunwayStateReportType;

/**
 * Created by rinne on 10/02/17.
 */
public class SnowClosure extends PrioritizedLexemeVisitor {
    public SnowClosure(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if ("SNOCLO".equalsIgnoreCase(token.getTACToken()) || "R/SNOCLO".equals(token.getTACToken())) {
            HashMap<RunwayStateReportType, Object> values = new HashMap<RunwayStateReportType, Object>();
            values.put(RunwayStateReportType.SNOW_CLOSURE, Boolean.TRUE);
            token.identify(RUNWAY_STATE);
            token.setParsedValue(VALUE, values);
        }
    }
}
