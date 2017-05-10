package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MINUTE1;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.TACReconstructorAdapter;

/**
 * Created by rinne on 10/02/17.
 */
public class IssueTime extends TimeHandlingRegex {

    public IssueTime(final Priority prio) {
        super("^([0-9]{2})([0-9]{2})([0-9]{2})Z$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        if (token.hasPrevious() && token.getPrevious().getIdentity() == AERODROME_DESIGNATOR) {
            int date = Integer.parseInt(match.group(1));
            int hour = Integer.parseInt(match.group(2));
            int minute = Integer.parseInt(match.group(3));
            if (timeOk(date, hour, minute)) {
                token.setParsedValue(DAY1, Integer.valueOf(date));
                token.setParsedValue(HOUR1, Integer.valueOf(hour));
                token.setParsedValue(MINUTE1, Integer.valueOf(minute));
                token.identify(ISSUE_TIME);
            } else {
                token.identify(ISSUE_TIME, Lexeme.Status.SYNTAX_ERROR, "Invalid date & time values");
            }
        }
    }

    public static class Reconstructor extends TACReconstructorAdapter {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, final Object specifier, final ParsingHints hints) {
            StringBuilder retval = new StringBuilder();
            int date = msg.getIssueDayOfMonth();
            int hour = msg.getIssueHour();
            int minute = msg.getIssueMinute();
            if (timeOk(date, hour, minute)) {
                retval.append(String.format("%02d", date));
                retval.append(String.format("%02d", hour));
                retval.append(String.format("%02d", minute));
                if ("UTC".equals(msg.getIssueTimeZone())) {
                    retval.append('Z');
                } else {
                    retval.append(msg.getIssueTimeZone());
                }
            }
            return this.getLexingFactory().createLexeme(retval.toString(), Lexeme.Identity.ISSUE_TIME);
        }
    }

}
