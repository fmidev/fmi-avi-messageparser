package fi.fmi.avi.parser.impl.lexer;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.LexemeVisitor;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.ParsingHints;

/**
 * Created by rinne on 10/02/17.
 */

public class LexingFactoryImpl implements LexingFactory {

    @Override
    public LexemeSequence createLexemeSequence(final String input, final ParsingHints hints) {
        LexemeSequenceImpl result = new LexemeSequenceImpl(input);
        appendArtifialStartTokenIfNecessary(input, result, hints);
        return result;
    }

    @Override
    public LexemeSequenceBuilder createLexemeSequenceBuilder() {
        return new LexemeSequenceBuilderImpl();
    }

    @Override
    public Lexeme createLexeme(final String token) {
        return new LexemeImpl(token);
    }

    @Override
    public Lexeme createLexeme(final String token, final Lexeme.Identity identity) {
        return new LexemeImpl(token, identity);
    }

    @Override
    public Lexeme createLexeme(final String token, final Lexeme.Identity identity, final Lexeme.Status status) {
        return new LexemeImpl(token, identity, status);
    }

    private static void appendArtifialStartTokenIfNecessary(final String input, final LexemeSequenceImpl result, final ParsingHints hints) {
        if (hints != null && hints.containsKey(ParsingHints.KEY_MESSAGE_TYPE)) {
            LexemeImpl artificialStartToken = null;
            if (hints.get(ParsingHints.KEY_MESSAGE_TYPE) == ParsingHints.VALUE_MESSAGE_TYPE_METAR && !input.startsWith("METAR ")) {
                artificialStartToken = new LexemeImpl("METAR", Lexeme.Identity.METAR_START);
            } else if (hints.get(ParsingHints.KEY_MESSAGE_TYPE) == ParsingHints.VALUE_MESSAGE_TYPE_TAF && !input.startsWith("TAF ")) {
                artificialStartToken = new LexemeImpl("TAF", Lexeme.Identity.TAF_START);
            }
            if (artificialStartToken != null) {
                artificialStartToken.setSynthetic(true);
                result.addAsFirst(artificialStartToken);
            }
        }
    }

    private static class LexemeSequenceImpl implements LexemeSequence {

        private String originalTac;
        private LinkedList<LexemeImpl> lexemes;
        private int changeCount = 0;

        public LexemeSequenceImpl(final String originalTac) {
            this();
            this.originalTac = originalTac;
            this.constructFromTAC();
        }

        public LexemeSequenceImpl() {
            this.lexemes = new LinkedList<LexemeImpl>();
        }

        @Override
        public String getTAC() {
            if (this.originalTac != null) {
                return this.originalTac;
            } else {
                return this.getAsTAC();
            }
        }

        @Override
        public int getTotalLexemeCount() {
            return this.lexemes.size();
        }

        @Override
        public Lexeme getFirstLexeme() {
            if (this.lexemes.size() > 0) {
                return this.lexemes.getFirst();
            } else {
                return null;
            }
        }

        @Override
        public Iterator<Lexeme> getLexemes() {
            return new RecognitionBasedLexemeIterator(this.getFirstLexeme(), null) {
                private final int changeCountAtStart = LexemeSequenceImpl.this.changeCount;

                @Override
                protected void modificationCheck() throws ConcurrentModificationException {
                    if (changeCount != changeCountAtStart) {
                        throw new ConcurrentModificationException("Lexeme sequence was modified while iterating");
                    }
                }
            };
        }

        @Override
        public Iterator<Lexeme> getUnrecognizedLexemes() {
            return new RecognitionBasedLexemeIterator(this.getFirstLexeme(), false) {
                private final int changeCountAtStart = LexemeSequenceImpl.this.changeCount;

                @Override
                protected void modificationCheck() throws ConcurrentModificationException {
                    if (changeCount != changeCountAtStart) {
                        throw new ConcurrentModificationException("Lexeme sequence was modified while iterating");
                    }
                }
            };
        }

        @Override
        public Iterator<Lexeme> getRecognizedLexemes() {
            return new RecognitionBasedLexemeIterator(this.getFirstLexeme(), true) {
                private final int changeCountAtStart = LexemeSequenceImpl.this.changeCount;

                @Override
                protected void modificationCheck() throws ConcurrentModificationException {
                    if (changeCount != changeCountAtStart) {
                        throw new ConcurrentModificationException("Lexeme sequence was modified while iterating");
                    }
                }
            };
        }

        LexemeImpl replaceFirstWith(final LexemeImpl replacement) {
            if (replacement == null) {
                throw new NullPointerException();
            }
            if (this.lexemes.size() == 0) {
                throw new IllegalStateException("No first lexeme to replace");
            }
            LexemeImpl oldFirst = this.lexemes.removeFirst();
            this.changeCount++;
            this.addAsFirst(replacement);
            return oldFirst;
        }

        LexemeImpl replaceLastWith(final LexemeImpl replacement) {
            if (replacement == null) {
                throw new NullPointerException();
            }
            if (this.lexemes.size() == 0) {
                throw new IllegalStateException("No last lexeme to replace");
            }
            LexemeImpl oldLast = this.lexemes.removeLast();
            this.changeCount++;
            this.addAsLast(replacement);
            return oldLast;
        }

        void addAsFirst(final LexemeImpl toAdd) {
            if (toAdd != null) {
                LexemeImpl oldFirst = this.lexemes.getFirst();
                if (oldFirst != null) {
                    oldFirst.setPrevious(toAdd);
                    toAdd.setNext(oldFirst);
                }
                toAdd.setPrevious(null);
                this.lexemes.addFirst(toAdd);
                this.changeCount++;
                this.updateFirst();
            }
        }

        void addAsLast(final LexemeImpl toAdd) {
            if (toAdd != null) {
                if (this.lexemes.size() > 0) {
                    LexemeImpl oldLast = this.lexemes.getLast();
                    oldLast.setNext(toAdd);
                    toAdd.setPrevious(oldLast);
                    toAdd.setFirst(this.lexemes.getFirst());
                } else {
                    toAdd.setFirst(toAdd);
                    toAdd.setPrevious(null);
                }
                toAdd.setNext(null);
                this.lexemes.addLast(toAdd);
                this.changeCount++;
            }
        }

        LexemeImpl removeFirst() {
            LexemeImpl removed = this.lexemes.removeFirst();
            this.changeCount++;
            if (removed != null) {
                this.lexemes.getFirst().setPrevious(null);
                this.updateFirst();
            }
            return removed;
        }

        LexemeImpl removeLast() {
            LexemeImpl removed = this.lexemes.removeLast();
            this.changeCount++;
            if (removed != null) {
                this.lexemes.getLast().setNext(null);
            }
            return removed;
        }

        private void updateFirst() {
            LexemeImpl first = this.lexemes.getFirst();
            for (LexemeImpl l : this.lexemes) {
                l.setFirst(first);
            }
        }

        private void constructFromTAC() {
            if (this.originalTac != null && this.originalTac.length() > 0) {
                Pattern horVisFractionNumberPart1Pattern = Pattern.compile("^[0-9]*$");
                Pattern horVisFractionNumberPart2Pattern = Pattern.compile("^[0-9]*/[0-9]*[A-Z]{2}$");
                Pattern windShearRunwayPattern = Pattern.compile("^RWY([0-9]{2})?[LRC]?$");
                Splitter byWhiteSpace = Splitter.onPattern("\\s").trimResults().omitEmptyStrings();
                Iterable<String> tokens = byWhiteSpace.split(originalTac);
                String lastToken = null;
                String lastLastToken = null;
                for (String s : tokens) {
                    if (s.endsWith("=")) {
                        LexemeImpl l = new LexemeImpl(s.substring(0, s.length() - 1));
                        this.addAsLast(l);
                        this.addAsLast(new LexemeImpl("=", Lexeme.Identity.END_TOKEN));
                    } else if (lastToken != null && horVisFractionNumberPart2Pattern.matcher(s).matches() && horVisFractionNumberPart1Pattern.matcher(lastToken)
                            .matches()) {
                        // cases like "1 1/8SM", combine the two tokens:
                        this.replaceLastWith(new LexemeImpl(lastToken + " " + s));
                    } else if ("WS".equals(lastLastToken) && "ALL".equals(lastToken) && windShearRunwayPattern.matcher(s).matches()) {
                        // "WS ALL RWY" case: concat all three parts as the last token:
                        this.removeLast(); // ALL
                        this.replaceLastWith(new LexemeImpl("WS ALL RWY"));
                    } else if ("WS".equals(lastToken) && windShearRunwayPattern.matcher(s).matches()) {
                        // "WS RWY22L" case, concat the two parts as the last token:
                        this.replaceLastWith(new LexemeImpl("WS " + s));
                    } else {
                        this.addAsLast(new LexemeImpl(s));
                    }
                    lastLastToken = lastToken;
                    lastToken = s;
                }
                this.changeCount++;
            }
        }

        private String getAsTAC() {
            StringBuilder sb = new StringBuilder();
            Iterator<Lexeme> it = this.getRecognizedLexemes();
            while (it.hasNext()) {
                sb.append(it.next().getTACToken());
                sb.append(' ');
            }
            if (sb.length() > 0) {
                return sb.substring(0, sb.length() - 1);
            } else {
                return "";
            }
        }

    }

    private static class LexemeSequenceBuilderImpl implements LexemeSequenceBuilder {
        private LexemeSequenceImpl seq;

        LexemeSequenceBuilderImpl() {
            seq = new LexemeSequenceImpl();
        }

        @Override
        public LexemeSequenceBuilder append(final Lexeme lexeme) {
            this.seq.addAsLast(new LexemeImpl(lexeme));
            return this;
        }

        @Override
        public LexemeSequence build() {
            return seq;
        }
    }

    private static class LexemeImpl implements Lexeme {
        private Identity id;
        private String tacToken;
        private Status status;
        private String lexerMessage;
        private boolean isSynthetic;
        private Map<ParsedValueName, Object> parsedValues;

        //Lexing navigation:
        private Lexeme first;
        private Lexeme next;
        private Lexeme prev;

        LexemeImpl() {
            this(null, null, Status.UNRECOGNIZED);
        }

        LexemeImpl(final Lexeme lexeme) {
            this.tacToken = lexeme.getTACToken();
            this.id = lexeme.getIdentity();
            this.status = lexeme.getStatus();
            this.lexerMessage = lexeme.getLexerMessage();
            this.isSynthetic = lexeme.isSynthetic();
            this.parsedValues = lexeme.getParsedValues();

        }
        LexemeImpl(final String token) {
            this(token, null, Status.UNRECOGNIZED);
        }

        LexemeImpl(final String token, final Identity identity) {
            this(token, identity, Status.OK);
        }

        LexemeImpl(final String token, final Identity identity, final Status status) {
            this.tacToken = token;
            this.id = identity;
            this.status = status;
            this.isSynthetic = false;
            this.parsedValues = new HashMap<ParsedValueName, Object>();
        }

        @Override
        public Identity getIdentity() {
            return this.id;
        }

        @Override
        public Identity getIdentityIfAcceptable() throws IllegalStateException {
            if (Status.OK == this.status || Status.WARNING == this.status) {
                return this.id;
            } else {
                return null;
            }
        }

        @Override
        public Status getStatus() {
            return this.status;
        }

        @Override
        public String getLexerMessage() {
            return this.lexerMessage;
        }

        @Override
        public Object getParsedValue(ParsedValueName name) {
            return this.parsedValues.get(name);
        }

        public <T> T getParsedValue(ParsedValueName name, Class<T> clz) {
            Object val = this.getParsedValue(name);
            if (clz.isAssignableFrom(val.getClass())) {
                return (T) val;
            } else {
                throw new IllegalArgumentException("Cannot return value of type " + val.getClass() + " as " + clz);
            }
        }

        @Override
        public Map<ParsedValueName, Object> getParsedValues() {
            return this.parsedValues;
        }

        @Override
        public String getTACToken() {
            return this.tacToken;
        }

        @Override
        public Lexeme getFirst() {
            return this.first;
        }

        @Override
        public Lexeme getPrevious() {
            return this.prev;
        }

        @Override
        public Lexeme getNext() {
            return this.next;
        }

        @Override
        public boolean hasPrevious() {
            return this.prev != null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public boolean isSynthetic() {
            return isSynthetic;
        }

        @Override
        public boolean isRecognized() {
            return !Status.UNRECOGNIZED.equals(this.status);
        }

        void setIdentity(final Identity id) {
            this.id = id;
        }

        @Override
        public void identify(final Identity id) {
            identify(id, Status.OK, null);
        }

        @Override
        public void identify(final Identity id, final Status status) {
            identify(id, status, null);
        }

        @Override
        public void identify(final Identity id, final Status status, final String note) {
            this.id = id;
            this.status = status;
            this.lexerMessage = note;
        }

        @Override
        public void setStatus(final Status status) {
            this.status = status;
        }

        @Override
        public void setSynthetic(final boolean synthetic) {
            isSynthetic = synthetic;
        }

        @Override
        public void setParsedValue(ParsedValueName name, Object value) {
            this.parsedValues.put(name, value);
        }

        @Override
        public void setParsedValues(Map<ParsedValueName, Object> values) {
            this.parsedValues = values;
        }

        @Override
        public void setLexerMessage(final String msg) {
            this.lexerMessage = msg;
        }

        @Override
        public void setTACToken(final String token) {
            this.tacToken = token;
        }

        @Override
        public void accept(final LexemeVisitor visitor, final ParsingHints hints) {
            //Always acccept:
            if (visitor != null) {
                visitor.visit(this, hints);
            }
        }

        void setFirst(final Lexeme token) {
            this.first = token;
        }

        void setNext(final Lexeme token) {
            this.next = token;
        }

        void setPrevious(final Lexeme token) {
            this.prev = token;
        }

        public String toString() {
            return new StringBuilder().append(this.tacToken)
                    .append(' ')
                    .append('(')
                    .append(this.id)
                    .append(',')
                    .append(this.status)
                    .append(')')
                    .toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final LexemeImpl lexeme = (LexemeImpl) o;

            if (isSynthetic != lexeme.isSynthetic) {
                return false;
            }
            if (id != lexeme.id) {
                return false;
            }
            if (status != lexeme.status) {
                return false;
            }
            return lexerMessage != null ? lexerMessage.equals(lexeme.lexerMessage) : lexeme.lexerMessage == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (status != null ? status.hashCode() : 0);
            result = 31 * result + (lexerMessage != null ? lexerMessage.hashCode() : 0);
            result = 31 * result + (isSynthetic ? 1 : 0);
            return result;
        }
    }

    private static abstract class RecognitionBasedLexemeIterator implements Iterator<Lexeme> {
        private Boolean returnRecognized;
        private Lexeme next;
        private HashSet<Integer> visited = new HashSet<Integer>();

        RecognitionBasedLexemeIterator(Lexeme head, Boolean returnRecognized) {
            this.returnRecognized = returnRecognized;
            this.next = head;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public boolean hasNext() {
            modificationCheck();
            if (this.returnRecognized == null) {
                return this.next != null;
            } else {
                Lexeme l = this.next;
                while (l != null && returnRecognized != l.isRecognized() && l.hasNext()) {
                    l = l.getNext();
                }
                if (l == null) {
                    return false;
                } else {
                    return returnRecognized == l.isRecognized();
                }
            }
        }

        @Override
        public Lexeme next() {
            Lexeme retval = null;
            if (this.next == null) {
                throw new NoSuchElementException("No more lexemes!");
            }
            //return the next lexeme regardless of is it's recognized or not:
            if (this.returnRecognized == null) {
                modificationCheck();
                retval = this.next;
                if (!this.visited.add(Integer.valueOf(System.identityHashCode(retval)))) {
                    throw new RuntimeException("Circular reference in Lexeme sequence detected!");
                }

            } else {
                while (returnRecognized != this.next.isRecognized() && this.next.hasNext()) {
                    modificationCheck();
                    this.next = this.next.getNext();
                    if (!this.visited.add(Integer.valueOf(System.identityHashCode(this.next)))) {
                        throw new RuntimeException("Circular reference in Lexeme sequence detected!");
                    }
                }
                retval = this.next;
                if (returnRecognized != retval.isRecognized()) {
                    throw new NoSuchElementException("No more lexemes!");
                }
            }
            this.next = this.next.getNext();
            return retval;
        }

        protected abstract void modificationCheck() throws ConcurrentModificationException;

    }

    ;

}
