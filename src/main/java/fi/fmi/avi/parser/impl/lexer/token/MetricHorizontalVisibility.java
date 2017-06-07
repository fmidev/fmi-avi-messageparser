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
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.taf.TAFForecast;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Status;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class MetricHorizontalVisibility extends RegexMatchingLexemeVisitor {
	
	public static final int MAX_STATUE_MILE_DENOMINATOR = 16;
	
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
    public void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints) {
        int visibility = Integer.parseInt(match.group(1));
        String direction = match.group(2);
        if (direction != null) {
        	DirectionValue dv = DirectionValue.forCode(direction);
        	if (dv == null) {
        		token.identify(HORIZONTAL_VISIBILITY, Status.SYNTAX_ERROR, "Invalid visibility direction value '" + direction + "'");
        	} else {
        		token.identify(HORIZONTAL_VISIBILITY);
        		token.setParsedValue(DIRECTION, dv);
        	}
        } else {
        	token.identify(HORIZONTAL_VISIBILITY);
        }

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
        
    }

	public static class Reconstructor extends FactoryBasedReconstructor {

		@Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, final ConversionHints hints, Object... specifier)
                throws SerializingException {
            Lexeme retval = null;

			NumericMeasure visibility = null;
			RelationalOperator operator = null;
			
			boolean identified = false;
			TAFForecast taf = getAs(specifier, TAFForecast.class);
			if (taf != null) {
				identified = true;
				visibility = taf.getPrevailingVisibility();
				operator = taf.getPrevailingVisibilityOperator();
			}
			
			TrendForecast metarTrend = getAs(specifier, TrendForecast.class);
			if (!identified && metarTrend != null) {
				identified = true;
				visibility = metarTrend.getPrevailingVisibility();
				operator = metarTrend.getPrevailingVisibilityOperator();
			}
			
			if (!identified && clz.isAssignableFrom(Metar.class)) {
				Metar metar = (Metar)msg;
				
				// TODO: metar.getVisibility().getMinimumVisibility() 
				if (metar.getVisibility() != null) {
					identified = true;
					
					visibility = metar.getVisibility().getPrevailingVisibility();
					operator = metar.getVisibility().getPrevailingVisibilityOperator();
				}
			}

			if (visibility != null) {
				String str;
				
				if ("m".equals(visibility.getUom())) {
					str = createMetricIntegerVisibility(visibility, operator);
				} else if ("sm".equals(visibility.getUom())) {
					str = createStatuteMilesVisibility(visibility, operator);
				} else {
                    throw new SerializingException("Unknown unit of measure '" + visibility.getUom() + "' for visibility");
                }
				
				
				// TODO: directional visibility
				
				retval = this.createLexeme(str, Lexeme.Identity.HORIZONTAL_VISIBILITY);
			}
			return retval;
		}

		private String createMetricIntegerVisibility(NumericMeasure visibility, RelationalOperator operator) throws SerializingException {
            String str;

			int meters = visibility.getValue().intValue();
			if (meters < 0) {
                throw new SerializingException("Visibility " + meters + " must be positive");
            }

			if (operator == RelationalOperator.BELOW && meters <= 50) {
				str = "0000";
			} else if (operator == RelationalOperator.ABOVE && meters >= 9999) {
				str = "9999";
			} else {
				str = String.format("%04d", meters);
			}

			return str;
		}
		

		private String createStatuteMilesVisibility(NumericMeasure visibility, RelationalOperator operator) throws SerializingException {
            StringBuilder builder = new StringBuilder();
			
			int integerPart = (int)Math.floor(visibility.getValue());
			
			double parts = visibility.getValue() - (double)integerPart;
			
			if (parts > 1.0/(double)16) {
			
				if (integerPart > 0) {
					builder.append(String.format("%d ", integerPart));
				}
				
				
				builder.append(findClosestFraction(parts, 16));
			} else {
				builder.append(String.format("%d", integerPart));
			}
			
			builder.append("SM");
			
			return builder.toString();
		}

		public static String findClosestFraction(final double number, final int maxDenominator) {
			if (maxDenominator < 3) {
				throw new IllegalArgumentException("max denominator should be at least 3 to make any sense, you gave me "+maxDenominator);
			}
			
			if (number >= 1.0 || number <= 0.0) {
				throw new IllegalArgumentException("it only makes sense to find fractions for numbers between 0 and 1 (exclusive)");
			}
			
			Integer currentBestNumerator = null;
			Integer currentBestDenominator = null;
			Double currentBestDelta = null;
			
			double doubleEquivalencyFactor = 0.00000001d;
			
			for (int denominator = 2; denominator <= maxDenominator; denominator++) {
				
				for (int numerator = 1; numerator < denominator; numerator++) {
					double delta = Math.abs(number - (double)numerator/(double)denominator );
					
					boolean isNewBest = false;
					
					if (currentBestDelta == null) {
						isNewBest = true;
					} else if (delta < currentBestDelta && Math.abs(currentBestDelta - delta) > doubleEquivalencyFactor) {
						isNewBest = true;
					}
					
					if (isNewBest) {
						currentBestNumerator = numerator;
						currentBestDenominator = denominator;
						currentBestDelta = delta;
						/*
						if (currentBestDelta < doubleEquivalencyFactor) {
							break;
						}
						*/
					}
				}
			}
			
			return String.format("%d/%d", currentBestNumerator, currentBestDenominator);
		}
	}
}
