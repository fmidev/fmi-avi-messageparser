package fi.fmi.avi.tac.lexer.impl.token;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.tac.lexer.Lexeme.ParsedValueName.VALUE;

import java.util.HashMap;

import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.tac.lexer.Lexeme;
import fi.fmi.avi.tac.lexer.impl.PrioritizedLexemeVisitor;
import fi.fmi.avi.tac.lexer.impl.token.RunwayState.RunwayStateReportType;

/**
 * Created by rinne on 10/02/17.
 */
public class SnowClosure extends PrioritizedLexemeVisitor {
    public SnowClosure(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ConversionHints hints) {
        if ("SNOCLO".equalsIgnoreCase(token.getTACToken()) || "R/SNOCLO".equals(token.getTACToken())) {
            HashMap<RunwayStateReportType, Object> values = new HashMap<RunwayStateReportType, Object>();
            values.put(RunwayStateReportType.SNOW_CLOSURE, Boolean.TRUE);
            token.identify(RUNWAY_STATE);
            token.setParsedValue(VALUE, values);
        }
    }
}
