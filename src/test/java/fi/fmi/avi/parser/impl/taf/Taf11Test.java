package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Taf11Test extends AbstractAviMessageTest<String, TAFImpl> {

	@Override
	public String getJsonFilename() {
		return "taf/taf11.json";
	}
	
	@Override
	public String getMessage() {
		return
				"EVRA 301103Z 3012/3112 15013KT 8000 OVC008 " + 
				"TEMPO 3012/3015 14015G26KT 5000 -RA OVC010 " + 
				"FM301500 18012KT 9000 NSW SCT015 BKN020 " +
                "TEMPO 3017/3103 19020G33KT 3000 -SHRA BKN012CB BKN020=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "TAF ";
	}
	
	@Override
	public ParsingHints getLexerParsingHints() {
		return ParsingHints.TAF;
	}

    @Override
    public ParsingHints getParserParsingHints() {
        return ParsingHints.TAF;
    }

    @Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                SURFACE_WIND, HORIZONTAL_VISIBILITY, NO_SIGNIFICANT_WEATHER, CLOUD, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND,
                HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD, END_TOKEN
		};
	}

	@Override
    public Class<String> getMessageInputClass() {
        return String.class;
    }

    @Override
    public Class<TAFImpl> getMessageOutputClass() {
        return TAFImpl.class;
	}

}
