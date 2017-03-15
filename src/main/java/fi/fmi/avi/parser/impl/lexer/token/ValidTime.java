package fi.fmi.avi.parser.impl.lexer.token;

import fi.fmi.avi.parser.Lexeme;

/**
 * Created by rinne on 10/02/17.
 */
public class ValidTime extends TAFTimePeriod {

    public ValidTime(final Priority prio) {
        super(prio);
    }

    @Override
    protected Lexeme.Identity getRequiredPreceedingIdentity() {
        return Lexeme.Identity.ISSUE_TIME;
    }

    @Override
    protected Lexeme.Identity getRecognizedIdentity() {
        return Lexeme.Identity.VALID_TIME;
    }

}
