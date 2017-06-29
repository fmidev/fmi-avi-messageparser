package fi.fmi.avi.tac.lexer.impl.token;

import fi.fmi.avi.tac.lexer.Lexeme;

/**
 * Created by rinne on 10/02/17.
 */
public class ChangeForecastTimeGroup extends TAFTimePeriod {

    public ChangeForecastTimeGroup(Priority priority) {
        super(priority);
    }

    @Override
    protected Lexeme.Identity getRequiredPreceedingIdentity() {
        return Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
    }

    @Override
    protected Lexeme.Identity getRecognizedIdentity() {
        return Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
    }


}
