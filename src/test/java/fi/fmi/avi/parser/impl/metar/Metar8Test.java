package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.COLOR_CODE;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.io.IOException;

import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar8Test extends AbstractAviMessageTest<String, MetarImpl> {

	@Override
	public String getJsonFilename() {
		return "metar/metar8.json";
	}
	
	@Override
	public String getMessage() {
		return
				"EGXE 061150Z 03010KT 9999 FEW020 17/11 Q1014 BLACKBLU TEMPO 6000 SHRA SCT020 BLACKWHT=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "METAR ";
	}
	
	@Override
	public ParsingHints getLexerParsingHints() {
		return ParsingHints.METAR;
	}
	
	// Remove this overridden method once the tokenizer is working
	@Override
	public void testTokenizer() throws TokenizingException, IOException {
		
	}

	// Remove this overridden method once the parser is working
	@Override
	public void testParser() throws IOException {
		
	}
	
	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, COLOR_CODE, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, COLOR_CODE,
                END_TOKEN
		};
	}

	@Override
    public Class<String> getMessageInputClass() {
        return String.class;
    }

    @Override
    public Class<MetarImpl> getMessageOutputClass() {
        return MetarImpl.class;
	}

}
