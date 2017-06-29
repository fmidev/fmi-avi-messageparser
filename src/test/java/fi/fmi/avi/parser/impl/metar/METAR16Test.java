package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.AUTOMATED;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.tac.lexer.Lexeme.Identity.SURFACE_WIND;

import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.metar.impl.METARImpl;
import fi.fmi.avi.converter.ConversionSpecification;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;
import fi.fmi.avi.tac.lexer.Lexeme.Identity;

public class METAR16Test extends AbstractAviMessageTest<String, METAR> {

	@Override
	public String getJsonFilename() {
		return "metar/metar16.json";
	}
	
	@Override
	public String getMessage() {
		return
				"METAR EFTU 011350Z AUTO VRB02KT 9999 ////// 22/12 Q1008=";
	}
	
	@Override
	public String getCanonicalMessage() {
		return
				"METAR EFTU 011350Z AUTO VRB02KT 9999 22/12 Q1008=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}

	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE,
                AIR_PRESSURE_QNH, END_TOKEN
		};
	}

	@Override
    public ConversionSpecification<String, METAR> getParsingSpecification() {
        return ConversionSpecification.TAC_TO_METAR_POJO;
    }
	
	@Override
    public ConversionSpecification<METAR, String> getSerializationSpecification() {
        return ConversionSpecification.METAR_POJO_TO_TAC;
    }

	@Override
    public Class<? extends METAR> getTokenizerImplmentationClass() {
        return METARImpl.class;
    }

}
