package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;

import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class Nil extends PrioritizedLexemeVisitor {

    public Nil(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getPrevious() != null && token.getPrevious().getIdentity() == ISSUE_TIME && "NIL".equalsIgnoreCase(token.getTACToken())) {
            token.identify(NIL);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final ParsingHints hints, final Object... specifier) {
            Lexeme retval = null;
            if (Metar.class.isAssignableFrom(clz)) {
                if (AviationCodeListUser.MetarStatus.MISSING == ((Metar) msg).getStatus()) {
                    retval = this.createLexeme("NIL", NIL);
                }
            } else if (TAF.class.isAssignableFrom(clz)) {
                if (AviationCodeListUser.TAFStatus.MISSING == ((TAF) msg).getStatus()) {
                    retval = this.createLexeme("NIL", NIL);
                }
            }
            return retval;
        }
    }
}
