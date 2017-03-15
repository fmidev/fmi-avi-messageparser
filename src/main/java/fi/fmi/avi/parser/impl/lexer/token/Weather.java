package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.RECENT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

/**
 * Created by rinne on 10/02/17.
 */
public class Weather extends RegexMatchingLexemeVisitor {

    public enum WeatherCodePart {
        LOW_INTENSITY("-"),
        HIGH_INTENSITY("+"),
        IN_VICINITY("VI"),
        SHALLOW("MI"),
        PATCHES("BC"),
        PARTIAL("PR"),
        DRIFTING("DR"),
        BLOWING("BL"),
        SHOWER("SH"),
        THUNDERSTORM("TS"),
        FREEZING("FZ"),
        DRIZZLE("DZ"),
        RAIN("RA"),
        SNOW("SN"),
        SNOW_GRAINS("SG"),
        ICE_CRYSTALS("IC"),
        PELLETS("PL"),
        HAIL("GR"),
        SNOW_PELLETS("GS"),
        UNKNOWN_PRECIPITATION("UP"),
        MIST("BR"),
        FOG("FG"),
        SMOKE("FU"),
        VOLCANIC_ASH("VA"),
        DUST("DU"),
        SAND("SA"),
        HAZE("HZ"),
        DUST_DEVILS("PO"),
        SQUALLS("SQ"),
        TORNADO_WATER_SPOUT("+FC"),
        FUNNEL_CLOUD("FC"),
        SANDSTORM("SS"),
        DUSTSTORM("DS");

        private final String code;

        WeatherCodePart(final String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public static WeatherCodePart forCode(final String code) {
            for (WeatherCodePart w : values()) {
                if (w.code.equals(code)) {
                    return w;
                }
            }
            return null;
        }
    }

    private final static Set<String> weatherCodeSkipWords = new HashSet<String>(Arrays.asList(
            new String[] { "METAR", "TAF", "COR", "AMD", "CNL", "NIL", "CAVOK", "TEMPO", "BECMG", "RMK", "NOSIG", "NSC", "NSW", "AUTO", "SNOCLO", "BLU", "WHT",
                    "GRN", "YLO1", "YLO2", "AMB", "RED" }));

    public Weather(final Priority prio) {
        super("^(RE)?([-+])?(VI)?([A-Z]{2,6})$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {
        boolean isPreceededWithAerodromeCode = false;
        boolean isPreceededWithRemarkStart = false;
        Lexeme l = token.getPrevious();
        while (l != null) {
            if (!isPreceededWithAerodromeCode && AERODROME_DESIGNATOR == l.getIdentityIfAcceptable()) {
                isPreceededWithAerodromeCode = true;
            }
            if (!isPreceededWithRemarkStart && REMARKS_START == l.getIdentityIfAcceptable()) {
                isPreceededWithRemarkStart = true;
            }
            l = l.getPrevious();
        }
        if (isPreceededWithRemarkStart) {
            return;
        }
        if (isPreceededWithAerodromeCode) {
            boolean isRecent = match.group(1) != null;
            String intensity = match.group(2);
            String vicinity = match.group(3);
            String weatherGroup = match.group(4);
            ArrayList<WeatherCodePart> values = new ArrayList<WeatherCodePart>();
            ArrayList<String> unrecognized = appendWeatherValues(intensity, vicinity, weatherGroup, values);
            if (unrecognized != null) {
                if (unrecognized.size() > 0) {
                    token.identify(isRecent ? RECENT_WEATHER : WEATHER, Lexeme.Status.SYNTAX_ERROR,
                            "Following weather codes were unrecognized: " + unrecognized.toString());
                } else {
                    token.identify(isRecent ? RECENT_WEATHER : WEATHER);
                    token.setParsedValue(Lexeme.ParsedValueName.VALUE, values);
                }
            }
        }
    }

    private static ArrayList<String> appendWeatherValues(final String intensity, final String vicinity, final String weatherGroup,
            final ArrayList<WeatherCodePart> values) {
        ArrayList<String> unrecognized = null;
        if (!weatherCodeSkipWords.contains(weatherGroup)) {
            unrecognized = new ArrayList<String>();
            WeatherCodePart part = WeatherCodePart.forCode(intensity);
            if (part != null) {
                values.add(part);
            }
            part = WeatherCodePart.forCode(vicinity);
            if (part != null) {
                values.add(part);
            }
            for (int i = 0; i <= weatherGroup.length() - 2; i = i + 2) {
                part = WeatherCodePart.forCode(weatherGroup.substring(i, i + 2));
                if (part != null) {
                    if (WeatherCodePart.FUNNEL_CLOUD == part && "+".equals(intensity)) {
                        values.add(WeatherCodePart.TORNADO_WATER_SPOUT);
                    } else {
                        values.add(part);
                    }
                } else {
                    unrecognized.add(weatherGroup.substring(i, i + 2));
                }
            }
        }
        return unrecognized;
    }
}
