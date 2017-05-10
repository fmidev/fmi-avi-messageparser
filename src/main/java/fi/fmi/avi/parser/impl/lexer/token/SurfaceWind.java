package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.DIRECTION;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MAX_VALUE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.MEAN_VALUE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.TrendForecastSurfaceWind;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFForecast;
import fi.fmi.avi.data.taf.TAFSurfaceWind;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;
import fi.fmi.avi.parser.impl.lexer.TACReconstructorAdapter;

/**
 * Created by rinne on 10/02/17.
 */
public class SurfaceWind extends RegexMatchingLexemeVisitor {

    public enum WindDirection {
        VARIABLE("VRB");

        private String code;

        WindDirection(final String code) {
            this.code = code;
        }

        public static WindDirection forCode(final String code) {
            for (WindDirection w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }
    }

    public SurfaceWind(final Priority prio) {
        super("^(VRB|[0-9]{3})([0-9]{2})(G[0-9]{2})?(KT|MPS|KMH)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        boolean formatOk = true;
        int direction = -1, mean, gustValue = -1;
        String unit;
        if (!"VRB".equals(match.group(1))) {
            direction = Integer.parseInt(match.group(1));
        }
        mean = Integer.parseInt(match.group(2));
        String gust = match.group(3);
        if (gust != null && 'G' == gust.charAt(0)) {
            try {
                gustValue = Integer.parseInt(gust.substring(1));
                if (gustValue < 0) {
                    formatOk = false;
                }
            } catch (NumberFormatException nfe) {
                formatOk = false;
            }
        }
        unit = match.group(4);
        if (direction >= 360 || mean < 0 || unit == null) {
            formatOk = false;
        }

        if (formatOk) {
            if (direction == -1) {
                token.setParsedValue(DIRECTION, WindDirection.VARIABLE);
            } else {
                token.setParsedValue(DIRECTION, Integer.valueOf(direction));
            }
            token.setParsedValue(MEAN_VALUE, Integer.valueOf(mean));
            if (gustValue > -1) {
                token.setParsedValue(MAX_VALUE, Integer.valueOf(gustValue));
            }
            token.setParsedValue(UNIT, unit.toLowerCase());
            token.identify(SURFACE_WIND);
        } else {
            token.identify(SURFACE_WIND, Lexeme.Status.SYNTAX_ERROR, "Wind direction or speed values invalid");
        }
    }

	public static class Reconstructor extends TACReconstructorAdapter {

		@Override
		public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, Object specifier, final ParsingHints hints) throws TokenizingException {
			Lexeme retval = null;
			TrendForecastSurfaceWind wind = null;
			
			if (specifier instanceof TAFForecast) {
				wind = ((TAFForecast) specifier).getSurfaceWind();
			}
			
			if (wind != null) {
				if (!wind.getMeanWindDirection().getUom().equals("deg")) {
					throw new TokenizingException("Mean wind direction unit is not 'deg': " + wind.getMeanWindDirection().getUom());
				}

				StringBuilder builder = new StringBuilder();

				builder.append(String.format("%03d", wind.getMeanWindDirection().getValue().intValue()));
				int speed = wind.getMeanWindSpeed().getValue().intValue();
				appendSpeed(builder, speed);

				if (wind.getWindGust() != null) {
					if (!wind.getWindGust().getUom().equals(wind.getMeanWindSpeed().getUom())) {
						throw new TokenizingException("Wind gust speed unit '" + wind.getWindGust().getUom() + "' is not the same as mean wind speed unit '" + wind.getMeanWindSpeed().getUom() + "'");
					}
					builder.append("G");
					appendSpeed(builder, wind.getWindGust().getValue().intValue());
				}

				builder.append(wind.getMeanWindSpeed().getUom().toUpperCase());

				retval = this.getLexingFactory().createLexeme(builder.toString(), Lexeme.Identity.SURFACE_WIND);
			
			}

			return retval;
		}

		private void appendSpeed(StringBuilder builder, int speed) throws TokenizingException {
			if (speed < 0 || speed >= 1000) {
				throw new TokenizingException("Wind speed value " + speed + " is now withing acceptable limits [0,1000]");
			} else if (speed >= 100) {
				builder.append(String.format("P%03d", speed));
			} else {
				builder.append(String.format("%02d", speed));
			}
		}
	}
}
