package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.COVER;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.TYPE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.ParsedValueName;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class CloudLayer extends RegexMatchingLexemeVisitor {

    public enum CloudCover {
        SKY_CLEAR("SKC"), NO_LOW_CLOUDS("CLR"), NO_SIG_CLOUDS("NSC"), FEW("FEW"), SCATTERED("SCT"), BROKEN("BKN"), OVERCAST("OVC"), SKY_OBSCURED("VV");

        private final String code;

        CloudCover(final String code) {
            this.code = code;
        }

        public static CloudCover forCode(final String code) {
            for (CloudCover w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }

    }

    public enum CloudType {
        TOWERING_CUMULUS("TCU"), CUMULONIMBUS("CB");

        private final String code;

        CloudType(final String code) {
            this.code = code;
        }

        public static CloudType forCode(final String code) {
            for (CloudType w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }

    }
    
    public enum SpecialValue {
    	AMOUNT_AND_HEIGHT_UNOBSERVABLE_BY_AUTO_SYSTEM;
    }

    public CloudLayer(final Priority prio) {
        super("^(([A-Z]{3}|VV)([0-9]{3})(CB|TCU)?)|(/{6})$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        if (match.group(5) != null) {
        	//Amount And Height Unobservable By Auto System
        	token.setParsedValue(ParsedValueName.VALUE, SpecialValue.AMOUNT_AND_HEIGHT_UNOBSERVABLE_BY_AUTO_SYSTEM);
        	token.identify(CLOUD);
        } else {
	    	CloudCover cloudCover = CloudCover.forCode(match.group(2));
	        int height = Integer.parseInt(match.group(3));
	        
	        if (cloudCover != null) {
	            token.identify(Lexeme.Identity.CLOUD);
	            token.setParsedValue(COVER, cloudCover);
	        } else {
	            token.identify(CLOUD, Lexeme.Status.SYNTAX_ERROR, "Unknown cloud cover " + match.group(2));
	        }
	        if (match.group(4) != null) {
	            token.setParsedValue(TYPE, CloudType.forCode(match.group(4)));
	        }
	        token.setParsedValue(VALUE, height);
	        token.setParsedValue(UNIT, "hft");
        }
    }
}
