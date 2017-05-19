package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.COVER;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.TYPE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationCodeListUser.CloudAmount;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.NumericMeasure;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.Lexeme.ParsedValueName;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
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
        	token.identify(CLOUD);
        	//Amount And Height Unobservable By Auto System
        	token.setParsedValue(ParsedValueName.VALUE, SpecialValue.AMOUNT_AND_HEIGHT_UNOBSERVABLE_BY_AUTO_SYSTEM);
        
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
    
    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(final T msg, Class<T> clz, final ParsingHints hints, final Object... specifier) throws TokenizingException {
            Lexeme retval = null;
            if (TAF.class.isAssignableFrom(clz)) {
            	fi.fmi.avi.data.CloudLayer layer = getAs(specifier, 0, fi.fmi.avi.data.CloudLayer.class);
            	String specialValue = getAs(specifier, 0, String.class);
            	TAFBaseForecast baseFct = getAs(specifier, 1, TAFBaseForecast.class);
            	TAFChangeForecast changeFct = getAs(specifier, 1, TAFChangeForecast.class);
            	if (baseFct != null || changeFct != null){
            		NumericMeasure verVis = null;
            		if ("VV".equals(specialValue)){
            			if (baseFct != null) {
            				verVis = baseFct.getCloud().getVerticalVisibility();
            			} else {
            				verVis = changeFct.getCloud().getVerticalVisibility();
            			}
            		}
					retval = this.createLexeme(getCloudLayerOrVerticalVisibilityToken(layer, verVis), Identity.CLOUD);
				}
            } else if (Metar.class.isAssignableFrom(clz)) {
            	//TODO
            }
            return retval;
        }
        
        private String getCloudLayerOrVerticalVisibilityToken(final fi.fmi.avi.data.CloudLayer layer, final NumericMeasure verVis) throws TokenizingException {
        	StringBuilder sb = new StringBuilder();
    		if (layer != null) {
    			NumericMeasure base = layer.getBase();
    			CloudAmount amount = layer.getAmount();
    			fi.fmi.avi.data.AviationCodeListUser.CloudType type = layer.getCloudType();
        		sb.append(amount.name());
        		sb.append(String.format("%03d", getAsHectoFeet(base)));
        		if (type != null) {
        			sb.append(type.name());
        		}
    		} else if (verVis != null) {
    			sb.append("VV");
    			sb.append(String.format("%03d", getAsHectoFeet(verVis)));
    		}
    		return sb.toString();
        }
        
        private long getAsHectoFeet(final NumericMeasure value) throws TokenizingException {
        	long hftValue = -1l;
        	if (value != null) {
				if ("hft".equalsIgnoreCase(value.getUom())) {
					hftValue = Math.round(value.getValue());
				} else if ("ft".equalsIgnoreCase(value.getUom())) {
					hftValue = Math.round(value.getValue() / 100.0);
				} else {
					throw new TokenizingException("Unable to reconstruct cloud layer / vertical visibility height with UoM '" + value.getUom() + "'");
				}
        	} else {
        		throw new TokenizingException("Unable to reconstruct cloud layer / vertical visibility height with null value");
        	}
			return hftValue;
        }
    }
}
