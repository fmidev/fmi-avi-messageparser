package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.AUTOMATED;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;

import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar16Test extends AbstractAviMessageTest<String, Metar> {

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
    public ConversionSpecification<String, Metar> getParserSpecification() {
        return ConversionSpecification.TAC_TO_METAR;
    }

    @Override
    public Class<? extends Metar> getTokenizerImplmentationClass() {
        return MetarImpl.class;
    }

}
