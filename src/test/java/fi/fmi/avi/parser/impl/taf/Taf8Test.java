package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.ParserSpecification;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Taf8Test extends AbstractAviMessageTest<String, TAF> {

	@Override
	public String getJsonFilename() {
		return "taf/taf8.json";
	}
	
	@Override
	public String getMessage() {
		return
				"EFVA 270930Z 2712/2812 14015G25KT 8000 -RA SCT020 OVC050 " + 
				"BECMG 2715/2717 5000 -RA BKN007 " + 
				"PROB40 2715/2720 4000 RASN " + 
				"BECMG 2720/2722 16012KT " + 
				"TEMPO 2720/2724 8000 " + 
				"PROB40 2802/2806 3000 RASN BKN004=";
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
				TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD,
                CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP,
                HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN
		};
	}

	@Override
    public ParserSpecification<String, TAF> getParserSpecification() {
        return ParserSpecification.TAC_TO_TAF;
    }

    @Override
    public Class<? extends TAF> getTokenizerImplmentationClass() {
        return TAFImpl.class;
    }

}
