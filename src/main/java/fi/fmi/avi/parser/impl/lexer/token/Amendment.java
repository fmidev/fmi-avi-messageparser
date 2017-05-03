package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AMENDMENT;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class Amendment extends PrioritizedLexemeVisitor {

    public Amendment(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getPrevious() == token.getFirst() && "AMD".equalsIgnoreCase(token.getTACToken())) {
            token.identify(AMENDMENT);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final Object specifier) {
            Lexeme retval = null;
            if (TAF.class.isAssignableFrom(clz)) {
                if (AviationCodeListUser.TAFStatus.AMENDMENT == ((TAF) msg).getStatus()) {
                    retval = this.getLexingFactory().createLexeme("AMD", AMENDMENT);
                }
            }
            return retval;
        }
    }
}
