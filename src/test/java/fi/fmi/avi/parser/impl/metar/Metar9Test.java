package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.*;

import java.io.IOException;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar9Test extends AbstractAviMessageTest {

	@Override
	public String getJsonFilename() {
		return "metar/metar9.json";
	}
	
	// Almost exactly the same as Metar 10 except WS ALL RWY
	@Override
	public String getMessage() {
		return
				"METAR EFHK 111111Z 15008KT 0700 R04R/1500N R15/1000U R22L/1200N R04L/1000VP1500U SN VV006 M08/M10 Q1023 RESN" + "" + " WS ALL RWY TEMPO 0900=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}
	
	@Override
	public ParsingHints getLexerParsingHints() {
		return ParsingHints.METAR;
	}
	
	// Remove this overridden method once the tokenizer is working
	@Override
	public void testTokenizer() throws TokenizingException, IOException {
		
	}
	
	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                RECENT_WEATHER, WIND_SHEAR, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, END_TOKEN
		};
	}
	
	@Override
	public Class<? extends AviationWeatherMessage> getMessageClass() {
		return MetarImpl.class;
	}

}
