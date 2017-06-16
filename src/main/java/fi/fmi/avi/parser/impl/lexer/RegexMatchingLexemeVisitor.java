package fi.fmi.avi.parser.impl.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;

/**
 * Created by rinne on 18/01/17.
 */
public abstract class RegexMatchingLexemeVisitor extends PrioritizedLexemeVisitor {

    private Pattern pattern;

    public RegexMatchingLexemeVisitor(final String pattern) {
        this(pattern, Priority.NORMAL);
    }

    public RegexMatchingLexemeVisitor(final String pattern, final Priority priority) {
        super(priority);
        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
		return pattern;
	}
    
    @Override
    public final void visit(final Lexeme token, final ConversionHints hints) {
        Matcher m = this.pattern.matcher(token.getTACToken());
        if (m.matches()) {
            this.visitIfMatched(token, m, hints);
        }
    }

    public abstract void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints);
}
