package fi.fmi.avi.parser.impl;

import fi.fmi.avi.parser.Lexeme;

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

    @FunctionalInterface
    interface LexemeParsingConsumer {
        void consume(final Lexeme lexeme);
    }

    @FunctionalInterface
    interface LexemeParsingNotifyer {
        void ping();
    }
}
