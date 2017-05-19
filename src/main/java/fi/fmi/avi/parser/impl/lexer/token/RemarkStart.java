package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.REMARK;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;

import java.util.ArrayList;
import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class RemarkStart extends PrioritizedLexemeVisitor {
    public RemarkStart(final Priority prio) {
        super(prio);
    }

    @Override
    public void visit(final Lexeme token, final ParsingHints hints) {
        if ("RMK".equalsIgnoreCase(token.getTACToken())) {
            token.identify(REMARKS_START);
        }
    }
    
    public static class Reconstructor extends FactoryBasedReconstructor {
    	@Override
    	public <T extends AviationWeatherMessage> List<Lexeme> getAsLexemes(T msg, Class<T> clz, ParsingHints hints,
    			Object... specifier) throws TokenizingException {
    		List<Lexeme> retval = null;
    		
    		if (msg.getRemarks() != null && !msg.getRemarks().isEmpty()) {
    			retval = new ArrayList<>();
    			retval.add(this.createLexeme("RMK", REMARKS_START));
    			for (String remark : msg.getRemarks()) {
    				retval.add(this.createLexeme(remark, REMARK));
    			}
    		}
    		
    		return retval;
    	}
    }
}
