package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.UNIT;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.regex.Matcher;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.NumericMeasure;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class AtmosphericPressureQNH extends RegexMatchingLexemeVisitor {

    public enum PressureMeasurementUnit {
        HECTOPASCAL("Q"), INCHES_OF_MERCURY("A");

        private String code;

        PressureMeasurementUnit(final String code) {
            this.code = code;
        }

        public static PressureMeasurementUnit forCode(final String code) {
            for (PressureMeasurementUnit w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }
    }

    public AtmosphericPressureQNH(final Priority prio) {
        super("^([AQ])([0-9]{4}|////)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints) {
        PressureMeasurementUnit unit = PressureMeasurementUnit.forCode(match.group(1));
        Integer value = null;
        if (!"////".equals(match.group(2))) {
            value = Integer.valueOf(match.group(2));
        }
        if (value != null) {
            token.identify(AIR_PRESSURE_QNH);
            token.setParsedValue(UNIT, unit);
            token.setParsedValue(VALUE, value);
        } else {
            token.identify(AIR_PRESSURE_QNH, Lexeme.Status.WARNING, "Missing value for air pressure");
        }
    }

    public static class Reconstructor extends FactoryBasedReconstructor {

        @Override
        public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ConversionHints hints, Object... specifier)
                throws SerializingException {
            Lexeme retval = null;

            NumericMeasure altimeter = null;

            if (clz.isAssignableFrom(Metar.class)) {
                Metar metar = (Metar) msg;

                altimeter = metar.getAltimeterSettingQNH();
            }

            if (altimeter != null) {
                if (altimeter.getValue() == null) {
                    throw new SerializingException("AltimeterSettingQNH is missing the value");
                }

                String unit = null;
                if ("hPa".equals(altimeter.getUom())) {
                    unit = "Q";
                } else if ("in Hg".equals(altimeter.getUom())) {
                    unit = "A";
                } else {
                    throw new SerializingException("Unknown unit of measure in AltimeterSettingQNH '" + altimeter.getUom() + "'");
                }

                StringBuilder builder = new StringBuilder();
                builder.append(unit);

                builder.append(String.format("%04d", altimeter.getValue().intValue()));

                retval = this.createLexeme(builder.toString(), Identity.AIR_DEWPOINT_TEMPERATURE);
            }

            return retval;
        }
    }
}
