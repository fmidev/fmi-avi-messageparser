package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;

import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.metar.impl.METARImpl;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class METAR20Test extends AbstractAviMessageTest<String, METAR> {

	@Override
	public String getJsonFilename() {
		return "metar/metar20.json";
	}
	
	@Override
	public String getMessage() {
		return
				"METAR EFTU 011350Z VRB02KT CAVOK " +
				"22/12 Q1008 " +
				"15R//////=";
	}
	
	@Override
	public String getCanonicalMessage() {
		return
				"METAR EFTU 011350Z VRB02KT CAVOK " +
				"22/12 Q1008 " +
				"65//////=";
	}
	
	@Override
	public ConversionHints getTokenizerParsingHints() {
		ConversionHints ret = new ConversionHints(ConversionHints.KEY_SERIALIZATION_POLICY, ConversionHints.VALUE_SERIALIZATION_POLICY_ANNEX3_16TH);
		return ret;
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}

	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, CAVOK,
				AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, 
				RUNWAY_STATE, END_TOKEN
		};
	}

    @Override
    public ConversionSpecification<String, METAR> getParserSpecification() {
        return ConversionSpecification.TAC_TO_METAR_POJO;
    }

    @Override
    public Class<? extends METAR> getTokenizerImplmentationClass() {
        return METARImpl.class;
    }

}
