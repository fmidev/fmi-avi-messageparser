package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.*;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar1Test extends AbstractAviMessageTest {

	@Override
	public String getJsonFilename() {
		return "metar/metar1.json";
	}
	
	@Override
	public String getMessage() {
		return
				"METAR EFHK 012400Z 00000KT 4500 R04R/0500D R15/0600VP1500D R22L/0275N R04L/P1500D BR FEW003 SCT050 14/13 Q1008 " +
				"TEMPO 2000=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}
	
	@Override
	public ParsingHints getLexerParsingHints() {
		return ParsingHints.METAR;
	}
	
	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, END_TOKEN
		};
	}
	
	@Override
	public Class<? extends AviationWeatherMessage> getMessageClass() {
		return MetarImpl.class;
	}

}
