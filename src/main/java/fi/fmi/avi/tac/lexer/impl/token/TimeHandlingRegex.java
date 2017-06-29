package fi.fmi.avi.tac.lexer.impl.token;

import fi.fmi.avi.tac.lexer.impl.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 22/02/17.
 */
public abstract class TimeHandlingRegex extends RegexMatchingLexemeVisitor {

    public TimeHandlingRegex(final String pattern, final Priority priority) {
        super(pattern, priority);
    }

    static boolean timeOk(int date, int hour) {
        return timeOk(date, hour, -1);
    }

    static boolean timeOk(int date, int hour, int minute) {
        if (date < 32 && hour < 25 && minute < 60) {
            if (hour == 24 && minute > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
