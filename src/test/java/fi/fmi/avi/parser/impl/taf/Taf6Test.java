package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
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
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Taf6Test extends AbstractAviMessageTest<String, TAF> {

	@Override
	public String getJsonFilename() {
		return "taf/taf6.json";
	}
	
	@Override
	public String getMessage() {
		return
				"TAF EFKU 190830Z 1909/2009 23010KT CAVOK " + 
				"PROB30 TEMPO 1915/1919 7000 SHRA SCT030CB BKN045 " + 
				"BECMG 1923/2001 30010KT=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}

	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, CAVOK, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, END_TOKEN
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
