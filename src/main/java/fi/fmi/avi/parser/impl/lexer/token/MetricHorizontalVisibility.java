package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RELATIONAL_OPERATOR;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationCodeListUser.RelationalOperator;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.NumericMeasure;
import fi.fmi.avi.data.taf.TAFForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Status;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;
import fi.fmi.avi.parser.impl.lexer.TACReconstructorAdapter;

/**
 * Created by rinne on 10/02/17.
 */
public class MetricHorizontalVisibility extends RegexMatchingLexemeVisitor {
	
	public enum DirectionValue {
		NORTH("N", 0),
		SOUTH("S", 180),
		EAST("E", 90),
		WEST("W", 270),
		NORTH_EAST("NE", 45),
		NORTH_WEST("NW", 315),
		SOUTH_EAST("SE", 135),
		SOUTH_WEST("SW", 225),
        NO_DIRECTIONAL_VARIATION("NDV",-1);

        private String code;
        private int deg;

        DirectionValue(final String code, final int deg) {
            this.code = code;
            this.deg = deg;
        }

        public static DirectionValue forCode(final String code) {
            for (DirectionValue w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }
        
        public int inDegrees() {
        	return this.deg;
        }

    }
	
    public MetricHorizontalVisibility(final Priority prio) {
        super("^([0-9]{4})([A-Z]{1,2}|NDV)?$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        int visibility = Integer.parseInt(match.group(1));
        String direction = match.group(2);
        token.setParsedValue(UNIT, "m");
        
        if (visibility == 9999) {
            token.setParsedValue(VALUE, Double.valueOf(10000d));
            token.setParsedValue(RELATIONAL_OPERATOR, RecognizingAviMessageTokenLexer.RelationalOperator.MORE_THAN);
        } else if (visibility == 0) {
            token.setParsedValue(VALUE, Double.valueOf(50d));
            token.setParsedValue(RELATIONAL_OPERATOR, RecognizingAviMessageTokenLexer.RelationalOperator.LESS_THAN);
        } else {
            token.setParsedValue(VALUE, Double.valueOf(visibility));
        }
        if (direction != null) {
        	DirectionValue dv = DirectionValue.forCode(direction);
        	if (dv != null) {
        		token.setParsedValue(DIRECTION, dv);
        	} else {
        		token.identify(HORIZONTAL_VISIBILITY, Status.SYNTAX_ERROR, "Invalid visibility direction value '" + direction + "'");
        		return;
        	}
        }
        token.identify(HORIZONTAL_VISIBILITY);
    }

	public static class Reconstructor extends TACReconstructorAdapter {

		@Override
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, Object specifier, final ParsingHints hints) {
			Lexeme retval = null;

			NumericMeasure visibility = null;
			RelationalOperator operator = null;

			if (specifier instanceof TAFForecast) {
				visibility = ((TAFForecast) specifier).getPrevailingVisibility();
				operator = ((TAFForecast) specifier).getPrevailingVisibilityOperator();
			}

			if (visibility != null) {
				String str;

				int meters = visibility.getValue().intValue();
				if (meters < 0 || meters > 1000) {
					// TODO: throw exception
				}

				if (operator == RelationalOperator.BELOW && meters <= 50) {
					str = "0000";
				} else if (operator == RelationalOperator.ABOVE && meters >= 1000) {
					str = "9999";
				} else {
					str = String.format("%04d", meters);
				}

				retval = this.getLexingFactory().createLexeme(str, Lexeme.Identity.HORIZONTAL_VISIBILITY);
			}
			return retval;
		}
	}
}
