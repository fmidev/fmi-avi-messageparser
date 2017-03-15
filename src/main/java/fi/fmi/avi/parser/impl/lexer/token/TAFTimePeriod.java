package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY2;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR2;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;

/**
 * Created by rinne on 22/02/17.
 */
public abstract class TAFTimePeriod extends TimeHandlingRegex {
    public TAFTimePeriod(final Priority prio) {
        super("^(([0-9]{2})([0-9]{2})([0-9]{2}))|(([0-9]{2})([0-9]{2})/([0-9]{2})([0-9]{2}))$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        if (token.hasPrevious() && token.getPrevious().getIdentity() == this.getRequiredPreceedingIdentity()) {
            if (match.group(1) != null) {
                //old 24h TAF, just one day field
                int day = Integer.parseInt(match.group(2));
                int fromHour = Integer.parseInt(match.group(3));
                int toHour = Integer.parseInt(match.group(4));
                if (timeOk(day, fromHour) && timeOk(day, toHour)) {
                    token.setParsedValue(DAY1, day);
                    token.setParsedValue(HOUR1, fromHour);
                    token.setParsedValue(HOUR2, toHour);
                    token.identify(this.getRecognizedIdentity());
                } else {
                    token.identify(this.getRecognizedIdentity(), Lexeme.Status.SYNTAX_ERROR, "Invalid date and/or time");
                }

            } else {
                //30h TAF
                int fromDay = Integer.parseInt(match.group(6));
                int fromHour = Integer.parseInt(match.group(7));
                int toDay = Integer.parseInt(match.group(8));
                int toHour = Integer.parseInt(match.group(9));
                if (timeOk(fromDay, fromHour) && timeOk(toDay, toHour)) {
                    token.setParsedValue(DAY1, fromDay);
                    token.setParsedValue(DAY2, toDay);
                    token.setParsedValue(HOUR1, fromHour);
                    token.setParsedValue(HOUR2, toHour);
                    token.identify(this.getRecognizedIdentity());
                } else {
                    token.identify(this.getRecognizedIdentity(), Lexeme.Status.SYNTAX_ERROR, "Invalid date and/or time");
                }
            }
        }
    }

    protected abstract Lexeme.Identity getRequiredPreceedingIdentity();

    protected abstract Lexeme.Identity getRecognizedIdentity();

}
