package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DAY2;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR1;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.HOUR2;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.data.AviationCodeListUser.TAFChangeIndicator;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;

/**
 * Created by rinne on 22/02/17.
 */
public abstract class TAFTimePeriod extends TimeHandlingRegex {
	
	private static final Logger LOG = LoggerFactory.getLogger(TAFTimePeriod.class);

	protected static String encodeTimePeriod(final int startDay, final int startHour, final int endDay, final int endHour, final ConversionHints hints)
	{
		String retval = null;
		boolean useShortFormat = false;
		try {
			if (hints != null) {
				Object hint = hints.get(ConversionHints.KEY_VALIDTIME_FORMAT);
				if (ConversionHints.VALUE_VALIDTIME_FORMAT_PREFER_SHORT.equals(hint)) {
					int numberOfHours = calculateNumberOfHours(startDay, startHour, endDay, endHour);
					if (numberOfHours < 24) {
						useShortFormat = true;
					}
				}
			}
		} catch(IllegalArgumentException iae) {
			LOG.info("Unable to determine whether to use long format or not", iae);
		}
		if (endDay > 0) {
			//If the valid time period is < 24h and the short format is preferred, is the short format
			if (useShortFormat) {
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

	/**
	 * This is trickier than one might expect. There are first of all special cases where
	 * people write numbers that look alright but are difficult for computer. Like 0700/0724
	 * Another class of problematic timecodes is where the days roll over to the next month.
	 * This tool has no context to determine which month and how many days there are supposed
	 * to be in it. However because of the domain, we make the assumption that endDay is
	 * startDay + 1 unless startDay < 28. If startDay < 28 => throw an exception as we cannot
	 * make assumptions on the length of month.
	 * 
	 * @param startDay the day-of-month of the period start
	 * @param startHour the 24h hour of the period start
	 * @param endDay the day-of-month of the period end
	 * @param endHour the 24h hour of the period end
	 * @return the time span as number of hours
	 * @throws IllegalArgumentException if the endDay smaller than startDay and startDay is less than 28
	 */
	static int calculateNumberOfHours(int startDay, int startHour, int endDay, int endHour)
	{
		// Store original parameters for exception texts
		String dateStr = String.format("%02d%02d/%02d%02d", startDay, startHour, endDay, endHour);
		
		if (endHour == 24) {
			endHour = 0;
			endDay++;
		}
		
		if (endDay < startDay) {
			if (startDay < 28) {
				throw new IllegalArgumentException("Unable to calculate number of hours between two time periods "+dateStr);
			}
			
			// Make the assumption that startDay is the last day in that month. This
			// assumption is based on the domain: aviation forecasts will not span more than 48 hours
			endDay += startDay;
		}
		
		int startN = startDay * 24 + startHour;
		int endN = endDay * 24 + endHour;
		
		return endN - startN;
	}
	
	
    public TAFTimePeriod(final Priority prio) {
        super("^(([0-9]{2})([0-9]{2})([0-9]{2}))|(([0-9]{2})([0-9]{2})/([0-9]{2})([0-9]{2}))$", prio);
    }

    @Override
	public void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints) {
		if (token.hasPrevious() && token.getPrevious().getIdentity() == this.getRequiredPreceedingIdentity()) {
            if (match.group(1) != null) {
                //old 24h TAF, just one day field
                int day = Integer.parseInt(match.group(2));
                int fromHour = Integer.parseInt(match.group(3));
                int toHour = Integer.parseInt(match.group(4));
                if (timeOk(day, fromHour) && timeOk(day, toHour)) {
                	token.identify(this.getRecognizedIdentity());
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
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ConversionHints hints, Object... specifier)
				throws SerializingException {
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
