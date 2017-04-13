package fi.fmi.avi.parser.impl;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingException;

/**
 * Created by rinne on 13/04/17.
 */
public abstract class AbstractAviMessageParser {

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt) {
        try {
            return findNext(needle, from, stopAt, null, null);
        } catch (ParsingException pe) {
            //Will never happen
        }
        return null;
    }

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found)
            throws ParsingException {
        return findNext(needle, from, stopAt, found, null);
    }

    protected static Lexeme findNext(final Lexeme.Identity needle, final Lexeme from, final Lexeme.Identity[] stopAt, final LexemeParsingConsumer found,
            final LexemeParsingNotifyer notFound) throws ParsingException {
        Lexeme retval = null;
        Lexeme current = from.getNext();
        if (current != null) {
            boolean stop = false;
            Lexeme.Identity currentId;
            while (!stop) {
                currentId = current.getIdentityIfAcceptable();
                if (currentId == needle) {
                    retval = current;
                }
                stop = !current.hasNext() || retval != null;
                if (stopAt != null) {
                    for (Lexeme.Identity i : stopAt) {
                        if (i == currentId) {
                            stop = true;
                            break;
                        }
                    }
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

    @FunctionalInterface
    interface LexemeParsingConsumer {
        void consume(final Lexeme lexeme) throws ParsingException;
    }

    @FunctionalInterface
    interface LexemeParsingNotifyer {
        void ping() throws ParsingException;
    }
}
