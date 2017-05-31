package fi.fmi.avi.parser.impl.lexer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.ParsingHints;

/**
 * Created by rinne on 21/12/16.
 */
public class AviMessageLexerImpl implements AviMessageLexer {
    private static final Logger LOG = LoggerFactory.getLogger(AviMessageLexerImpl.class);
    private static final int MAX_ITERATIONS = 100;

    private Map<String, RecognizingAviMessageTokenLexer> tokenLexers = new HashMap<String, RecognizingAviMessageTokenLexer>();

    private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

    public void addTokenLexer(final String startTokenId, final RecognizingAviMessageTokenLexer l) {
        this.tokenLexers.put(startTokenId, l);
    }

    @Override
    public LexemeSequence lexMessage(final String input) {
        return this.lexMessage(input, null);
    }

    @Override
    public LexemeSequence lexMessage(final String input, final ParsingHints hints) {
        if (this.factory == null) {
            throw new IllegalStateException("LexingFactory not injected");
        }
        LexemeSequence result = this.factory.createLexemeSequence(input, hints);
        RecognizingAviMessageTokenLexer tokenLexer = tokenLexers.get(result.getFirstLexeme().getTACToken());
        if (tokenLexer != null) {
            boolean lexemesChanged = true;
            int iterationCount = 0;
            while (lexemesChanged && iterationCount < MAX_ITERATIONS) {
                lexemesChanged = false;
                iterationCount++;
                int oldHashCode;
                List<Lexeme> unrecognizedlexemes = result.getLexemes()
                        .stream()
                        .filter(l -> Lexeme.Status.UNRECOGNIZED == l.getStatus())
                        .collect(Collectors.toList());
                for (Lexeme lexeme : unrecognizedlexemes) {
                    oldHashCode = lexeme.hashCode();
                    lexeme.accept(tokenLexer, hints);
                    lexemesChanged = lexemesChanged || oldHashCode != lexeme.hashCode();
                }
            }
            if (iterationCount == MAX_ITERATIONS) {
                LOG.warn("Lexing result for " + result.getFirstLexeme().getIdentity() + " did not stabilize within the maximum iteration count "
                        + MAX_ITERATIONS + ", result may be incomplete");
            }
        }
        return result;
    }

}
