package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingIssue;

/**
 * Created by rinne on 13/04/17.
 */
public abstract class AbstractAviMessageParser {

	protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from) {
       return findNext(needle, from, null);
    }

    protected static Lexeme findNext(final Lexeme from, final Lexeme.Identity[] stopAt) {
        return findNext(null, from, stopAt);
    }
    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt) {
        return findNext(needle, from, stopAt, null, null);
    }

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found) {
        return findNext(needle, from, stopAt, found, null);
    }

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found,
            final LexemeParsingNotifyer notFound) {
        Lexeme retval = null;
        Lexeme current = from.getNext();
        if (current != null) {
            boolean stop = false;
            Lexeme.Identity currentId;
            while (!stop) {
                currentId = current.getIdentityIfAcceptable();
                if (stopAt != null) {
                    for (Lexeme.Identity i : stopAt) {
                        if (i == currentId) {
                            stop = true;
                            break;
                        }
                    }
                }
                if (!stop) {
                    if (needle == null || currentId == needle) {
                        retval = current;
                    }
                    stop = !current.hasNext() || retval != null;
                }
                current = current.getNext();
            }
        }
        if (retval != null) {
            if (found != null) {
                found.consume(retval);
            }
        } else {
            if (notFound != null) {
                notFound.ping();
            }
        }
        return retval;
    }

    protected static List<ParsingIssue> checkZeroOrOne(LexemeSequence lexed, Lexeme.Identity[] ids) {
        List<ParsingIssue> retval = new ArrayList<>();
        boolean[] oneFound = new boolean[ids.length];
        Iterator<Lexeme> it = lexed.getRecognizedLexemes();
        while (it.hasNext()) {
            Lexeme l = it.next();
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] == l.getIdentity()) {
                    if (!oneFound[i]) {
                        oneFound[i] = true;
                    } else {
                        retval.add(new ParsingIssue(ParsingIssue.Type.SYNTAX_ERROR, "More than one of " + l.getIdentity() + " in " + lexed.getTAC()));
                    }
                }
            }
        }
        return retval;
    }

    protected static List<ParsingIssue> updateIssueTime(final AviationWeatherMessage msg, final LexemeSequence lexed, final Lexeme.Identity[] before,
            final ParsingHints hints) {
        List<ParsingIssue> retval = new ArrayList<>();
        findNext(ISSUE_TIME, lexed.getFirstLexeme(), before, (match) -> {
            Integer day = match.getParsedValue(Lexeme.ParsedValueName.DAY1, Integer.class);
            Integer minute = match.getParsedValue(Lexeme.ParsedValueName.MINUTE1, Integer.class);
            Integer hour = match.getParsedValue(Lexeme.ParsedValueName.HOUR1, Integer.class);
            if (day != null && minute != null && hour != null) {
                msg.setIssueDayOfMonth(day);
                msg.setIssueHour(hour);
                msg.setIssueMinute(minute);
                msg.setIssueTimeZone("UTC");
            }
        }, () -> {
            retval.add(new ParsingIssue(ParsingIssue.Type.MISSING_DATA, "Missing at least some of the issue time components in " + lexed.getTAC()));
        });
        return retval;
    }

    @FunctionalInterface
    interface LexemeParsingConsumer {
        void consume(final Lexeme lexeme);
    }

    @FunctionalInterface
    interface LexemeParsingNotifyer {
        void ping();
    }
}
