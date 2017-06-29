package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.WEATHER;

import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.converter.ConversionSpecification;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;
import fi.fmi.avi.tac.lexer.Lexeme.Identity;

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
    public ConversionSpecification<String, TAF> getParsingSpecification() {
        return ConversionSpecification.TAC_TO_TAF_POJO;
    }
    
    @Override
    public ConversionSpecification<TAF, String> getSerializationSpecification() {
        return ConversionSpecification.TAF_POJO_TO_TAC;
    }


	@Override
	public Class<? extends TAF> getTokenizerImplmentationClass() {
		return TAFImpl.class;
	}

}
