package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class NoSignificantWeather extends PrioritizedLexemeVisitor {
    public NoSignificantWeather(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if ("NOSIG".equalsIgnoreCase(token.getTACToken()) || "NSW".equalsIgnoreCase(token.getTACToken())) {
            token.identify(NO_SIGNIFICANT_WEATHER);
        }
    }
}
