package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MINUTE1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.TYPE;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequenceBuilder;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;

/**
 * Created by rinne on 10/02/17.
 */
public class ForecastChangeIndicator extends TimeHandlingRegex {

	public enum ForecastChangeIndicatorType {
        TEMPORARY_FLUCTUATIONS("TEMPO"),
        BECOMING("BECMG"),
        WITH_40_PCT_PROBABILITY("PROB40"),
        WITH_30_PCT_PROBABILITY("PROB30"),
        AT("AT"),
        FROM("FM"),
        UNTIL("TL");

        private String code;

        ForecastChangeIndicatorType(final String code) {
            this.code = code;
        }

        public static ForecastChangeIndicatorType forCode(final String code) {
            for (ForecastChangeIndicatorType w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }

    }

    public ForecastChangeIndicator(final Priority prio) {
        super("^(TEMPO|BECMG|PROB40|PROB30)|((AT|FM|TL)([0-9]{2})?([0-9]{2})([0-9]{2}))$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        ForecastChangeIndicatorType indicator;
        if (match.group(1) != null) {
            indicator = ForecastChangeIndicatorType.forCode(match.group(1));
            token.setParsedValue(TYPE, indicator);
            token.identify(FORECAST_CHANGE_INDICATOR);
        } else {
            indicator = ForecastChangeIndicatorType.forCode(match.group(3));
            int day = -1;
            if (match.group(4) != null) {
                day = Integer.parseInt(match.group(4));
            }
            int hour = Integer.parseInt(match.group(5));
            int minute = Integer.parseInt(match.group(6));
            if (timeOk(day, hour, minute)) {
                if (day > -1) {
                    token.setParsedValue(DAY1, day);
                }
                token.setParsedValue(HOUR1, hour);
                token.setParsedValue(MINUTE1, minute);
                token.setParsedValue(TYPE, indicator);
                token.identify(FORECAST_CHANGE_INDICATOR);
            } else {
                token.identify(FORECAST_CHANGE_INDICATOR, Lexeme.Status.SYNTAX_ERROR, "Invalid time");
            }
        }
    }
    

    public static class Reconstructor extends FactoryBasedReconstructor {

		@Override
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ParsingHints hints,
				Object... specifier) throws TokenizingException {
			TAFChangeForecast changeForecast = getAs(specifier, TAFChangeForecast.class);
			
			Lexeme retval = null;
			
			String code = null;
			if (changeForecast != null) {
				
				switch (changeForecast.getChangeIndicator()) {
				case BECOMING:
					code = "BECMG";
					break;
				case TEMPORARY_FLUCTUATIONS:
					code = "TEMPO";
					break;
				case PROBABILITY_30:
					code = "PROB30";
					break;
				case PROBABILITY_40:
					code = "PROB40";
					break;
				case PROBABILITY_30_TEMPORARY_FLUCTUATIONS:
					code = "PROB30 TEMPO";
					break;
				case PROBABILITY_40_TEMPORARY_FLUCTUATIONS:
					code = "PROB40 TEMPO";
					break;
				case FROM:
					code = createCode_From(changeForecast);
					break;
				}
			}
			
			if (code != null) {
				retval = this.getLexingFactory().createLexeme(code, FORECAST_CHANGE_INDICATOR);
			}
			
			return retval;
		}

		private String createCode_From(TAFChangeForecast changeForecast) {
			StringBuilder ret = new StringBuilder("FM");
			ret.append(String.format("%02d%02d%02d", 
					changeForecast.getValidityStartDayOfMonth(),
					changeForecast.getValidityStartHour(),
					changeForecast.getValidityStartMinute()));
			
			return ret.toString();
		}

	}

}
