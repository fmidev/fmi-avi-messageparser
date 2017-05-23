package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.parser.Lexeme.Identity.*;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Taf15Test extends AbstractAviMessageTest {

	@Override
	public String getJsonFilename() {
		return "taf/taf15.json";
	}
	
	@Override
	public String getMessage() {
		return
				"TAF EFRO 062331Z 0700/0724 20009KT CAVOK " +
	    		"PROB30 0702/0706 1000 BCFG BKN001=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}
	
	@Override
	public ParsingHints getLexerParsingHints() {
		return ParsingHints.TAF;
	}
	
	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME,
        		VALID_TIME, SURFACE_WIND, CAVOK, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, 
        		HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN
		};
	}
	
	@Override
	public Class<? extends AviationWeatherMessage> getMessageClass() {
		return TAFImpl.class;
	}

}
