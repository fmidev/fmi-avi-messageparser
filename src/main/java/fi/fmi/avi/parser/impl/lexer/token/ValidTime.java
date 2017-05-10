package fi.fmi.avi.parser.impl.lexer.token;


import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.Period;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.TACReconstructorAdapter;

/**
 * Created by rinne on 10/02/17.
 */
public class ValidTime extends TAFTimePeriod {

    public ValidTime(final Priority prio) {
        super(prio);
    }

    @Override
    protected Lexeme.Identity getRequiredPreceedingIdentity() {
        return Lexeme.Identity.ISSUE_TIME;
    }

    @Override
    protected Lexeme.Identity getRecognizedIdentity() {
        return Lexeme.Identity.VALID_TIME;
    }

	public static class Reconstructor extends TACReconstructorAdapter {

		@Override
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, Object specifier, final ParsingHints hints) {
			Lexeme retval = null;
			if (TAF.class.isAssignableFrom(clz)) {
				TAF taf = (TAF) msg;
				String period = encodeTimePeriod(
						taf.getValidityStartDayOfMonth(), 
						taf.getValidityStartHour(), 
						taf.getValidityEndDayOfMonth(), 
						taf.getValidityEndHour(), 
						hints);
				retval = this.getLexingFactory().createLexeme(period, Lexeme.Identity.VALID_TIME);
			}

			return retval;
		}

	}
}
