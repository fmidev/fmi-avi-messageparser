package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY2;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR2;

import java.util.regex.Matcher;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.Period;

import fi.fmi.avi.data.AviationCodeListUser.TAFChangeIndicator;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;

/**
 * Created by rinne on 22/02/17.
 */
public abstract class TAFTimePeriod extends TimeHandlingRegex {
	
	protected static String encodeTimePeriod(final int startDay, final int startHour, final int endDay, final int endHour, final ParsingHints hints) {
		String retval = null;
		DateTimeFieldType[] dayHour = {DateTimeFieldType.dayOfMonth(), DateTimeFieldType.hourOfDay()};
		int [] start = {startDay, startHour};
		int [] end = {endDay, endHour};
		Period p = new Period(new Partial(dayHour, start), new Partial(dayHour, end));
		if (endDay > 0) {
			//If the valid time period is < 24h and the short format is preferred, is the short format
			if (hints != null && ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_SHORT.equals(hints.get(ParsingHints.KEY_VALIDTIME_FORMAT)) && p.toStandardHours().getHours() < 24) {
				retval = String.format("%02d%02d%02d",startDay,startHour,endHour);
			} else {
			// Otherwise produce validity in the (long) 2008 Nov TAF format
				retval = String.format("%02d%02d/%02d%02d",startDay,startHour,endDay,endHour);
			}
		} else {
			//End day not given, assume the short format
			retval = String.format("%02d%02d%02d",startDay,startHour,endHour);
		}
		return retval;
	}
	
    public TAFTimePeriod(final Priority prio) {
        super("^(([0-9]{2})([0-9]{2})([0-9]{2}))|(([0-9]{2})([0-9]{2})/([0-9]{2})([0-9]{2}))$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        if (token.hasPrevious() && token.getPrevious().getIdentity() == this.getRequiredPreceedingIdentity()) {
            if (match.group(1) != null) {
                //old 24h TAF, just one day field
                int day = Integer.parseInt(match.group(2));
                int fromHour = Integer.parseInt(match.group(3));
                int toHour = Integer.parseInt(match.group(4));
                if (timeOk(day, fromHour) && timeOk(day, toHour)) {
                    token.setParsedValue(DAY1, day);
                    token.setParsedValue(HOUR1, fromHour);
                    token.setParsedValue(HOUR2, toHour);
                    token.identify(this.getRecognizedIdentity());
                } else {
                    token.identify(this.getRecognizedIdentity(), Lexeme.Status.SYNTAX_ERROR, "Invalid date and/or time");
                }

            } else {
                //30h TAF
                int fromDay = Integer.parseInt(match.group(6));
                int fromHour = Integer.parseInt(match.group(7));
                int toDay = Integer.parseInt(match.group(8));
                int toHour = Integer.parseInt(match.group(9));
                if (timeOk(fromDay, fromHour) && timeOk(toDay, toHour)) {
                	token.identify(this.getRecognizedIdentity());
                	token.setParsedValue(DAY1, fromDay);
                    token.setParsedValue(DAY2, toDay);
                    token.setParsedValue(HOUR1, fromHour);
                    token.setParsedValue(HOUR2, toHour);
                } else {
                    token.identify(this.getRecognizedIdentity(), Lexeme.Status.SYNTAX_ERROR, "Invalid date and/or time");
                }
            }
        }
    }

    protected abstract Lexeme.Identity getRequiredPreceedingIdentity();

    protected abstract Lexeme.Identity getRecognizedIdentity();
    

    public static class Reconstructor extends FactoryBasedReconstructor {

		@Override
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ParsingHints hints,
				Object... specifier) throws TokenizingException {
			Lexeme retval = null;
			if (TAF.class.isAssignableFrom(clz)) {
				TAFChangeForecast forecast = getAs(specifier, TAFChangeForecast.class);

				// Skip FROM change indicators since validity time is indicated with the FM lexeme
				if (forecast != null && forecast.getChangeIndicator() != TAFChangeIndicator.FROM) {
					int validityEndDayOfMonth = forecast.getValidityEndDayOfMonth();
					if (validityEndDayOfMonth == -1) {
						validityEndDayOfMonth = forecast.getValidityStartDayOfMonth();
					}
					
					String str = String.format("%02d%02d/%02d%02d",
							forecast.getValidityStartDayOfMonth(), forecast.getValidityStartHour(),
							validityEndDayOfMonth, forecast.getValidityEndHour());

                    retval = this.createLexeme(str, Identity.CHANGE_FORECAST_TIME_GROUP);
                }
			}
			return retval;
		}
    	
    }
}
