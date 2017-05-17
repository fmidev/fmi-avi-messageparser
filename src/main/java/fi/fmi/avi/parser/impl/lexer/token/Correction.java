package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;

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
public class Correction extends PrioritizedLexemeVisitor {

    public Correction(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if (token.getPrevious() == token.getFirst() && "COR".equalsIgnoreCase(token.getTACToken())) {
            token.identify(CORRECTION);
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final ParsingHints hints, final Object... specifier) {
            Lexeme retval = null;
            if (Metar.class.isAssignableFrom(clz)) {
                if (AviationCodeListUser.MetarStatus.CORRECTION == ((Metar) msg).getStatus()) {
                    retval = this.createLexeme("COR", CORRECTION);
                }
            } else if (TAF.class.isAssignableFrom(clz)) {
                if (AviationCodeListUser.TAFStatus.CORRECTION == ((TAF) msg).getStatus()) {
                    retval = this.createLexeme("COR", CORRECTION);
                }
            }
            return retval;
        }
    }
}
