package fi.fmi.avi.parser.impl.lexer.token;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.Lexeme;

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

	public static class Reconstructor extends FactoryBasedReconstructor {

		@Override
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, Object specifier) {
			Lexeme retval = null;
			if (TAF.class.isAssignableFrom(clz)) {
				TAF taf = (TAF) msg;

				// Always produce validity in the 2008 Nov TAF format
				String str = String.format("%02d%02d/%02d%02d",
						taf.getValidityStartDayOfMonth(),
						taf.getValidityStartHour(),
						taf.getValidityEndDayOfMonth(),
						taf.getValidityEndHour());

				retval = this.getLexingFactory().createLexeme(str, Lexeme.Identity.VALID_TIME);
			}

			return retval;
		}

	}
}
