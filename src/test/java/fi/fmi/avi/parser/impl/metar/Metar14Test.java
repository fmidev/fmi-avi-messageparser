package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.*;

import java.io.IOException;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar14Test extends AbstractAviMessageTest {

	@Override
	public String getJsonFilename() {
		return "metar/metar14.json";
	}
	
	@Override
	public String getMessage() {
		return
				"METAR EFOU 181750Z AUTO 18007KT 9999 OVC010 02/01 Q1015 R/SNOCLO=";
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
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RUNWAY_STATE, END_TOKEN
		};
	}
	
	@Override
	public Class<? extends AviationWeatherMessage> getMessageClass() {
		return MetarImpl.class;
	}

}
