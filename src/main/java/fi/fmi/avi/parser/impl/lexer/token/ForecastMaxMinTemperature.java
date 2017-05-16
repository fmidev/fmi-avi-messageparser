package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.MAX_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.MIN_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFAirTemperatureForecast;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;

/**
 * Created by rinne on 10/02/17.
 */
public class ForecastMaxMinTemperature extends TimeHandlingRegex {

    public enum TemperatureForecastType {
        MINIMUM("TN"), MAXIMUM("TX");

        private final String code;

        TemperatureForecastType(final String code) {
            this.code = code;
        }

        public static TemperatureForecastType forCode(final String code) {
            for (TemperatureForecastType w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }

    }

    public ForecastMaxMinTemperature(final Priority prio) {
        super("^(TX|TN)(M)?([0-9]{2})/([0-9]{2})?([0-9]{2})(Z)?$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        TemperatureForecastType kind = TemperatureForecastType.forCode(match.group(1));
        boolean isNegative = match.group(2) != null;
        int value = Integer.parseInt(match.group(3));
        if (isNegative) {
            value = value * -1;
        }
        int day = -1;
        if (match.group(3) != null) {
            day = Integer.parseInt(match.group(4));
        }
        int hour = Integer.parseInt(match.group(5));
        if (timeOk(day, hour)) {
            if (day > -1) {
                token.setParsedValue(DAY1, day);
            }
            token.setParsedValue(HOUR1, hour);
            token.setParsedValue(VALUE, value);
            if (TemperatureForecastType.MAXIMUM == kind) {
                token.identify(MAX_TEMPERATURE);
            } else {
                token.identify(MIN_TEMPERATURE);
            }
        } else {
            if (TemperatureForecastType.MAXIMUM == kind) {
                token.identify(MAX_TEMPERATURE, Lexeme.Status.SYNTAX_ERROR, "Invalid day/hour values");
            } else {
                token.identify(MIN_TEMPERATURE, Lexeme.Status.SYNTAX_ERROR, "Invalid day/hour values");
            }
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {
    	@Override
    	public <T extends AviationWeatherMessage> List<Lexeme> getAsLexemes(T msg, Class<T> clz, ParsingHints hints,
    			Object... specifier) throws TokenizingException {
    		List<Lexeme> retval = new ArrayList<>();
    		
    		if (TAF.class.isAssignableFrom(clz)) {
    			
    			TAFBaseForecast forecast = getAs(specifier, TAFBaseForecast.class);
    			
    			if (forecast.getTemperatures() != null) {
    				for (TAFAirTemperatureForecast temp : forecast.getTemperatures()) {

    					if (temp.getMaxTemperature() != null) {
    						if (!"degC".equals(temp.getMaxTemperature().getUom())) {
    							throw new TokenizingException("Unsupported unit of measurement for maximum temperature: '"+temp.getMaxTemperature().getUom()+"'");
    						}
    						
    						String s = formatTemp("TX", 
    								temp.getMaxTemperature().getValue(), 
    								temp.getMaxTemperatureDayOfMonth(),
    								temp.getMaxTemperatureHour());

    						retval.add(this.getLexingFactory().createLexeme(s, MAX_TEMPERATURE));
    					}
    					
    					if (temp.getMinTemperature() != null) {
    						if (!"degC".equals(temp.getMinTemperature().getUom())) {
    							throw new TokenizingException("Unsupported unit of measurement for minimum temperature: '"+temp.getMaxTemperature().getUom()+"'");
    						}
    						
    						String s = formatTemp("TN", 
    								temp.getMinTemperature().getValue(), 
    								temp.getMinTemperatureDayOfMonth(),
    								temp.getMinTemperatureHour());

    						retval.add(this.getLexingFactory().createLexeme(s, MIN_TEMPERATURE));
    					}
    					
    				}
    			}
    		}
    		
    		return retval;
    	}
    	
    	public String formatTemp(String prefix, Double temp, int day, int hour) {
    		String s = String.format("%s%02d/%02d%02dZ",
					temp < 0.0 ? prefix + "M" : prefix,
					Math.abs(temp.intValue()),
					day,
					hour);
    		
    		return s;
    	}
    }
}
