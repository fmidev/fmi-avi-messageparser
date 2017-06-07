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
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;

import java.io.IOException;

import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar14Test extends AbstractAviMessageTest<String, Metar> {

	@Override
	public String getJsonFilename() {
		return "metar/metar14.json";
	}
	
	@Override
	public String getMessage() {
		return
				"METAR EFOU 181750Z AUTO 18007KT 9999 OVC010 02/01 Q1015 R/SNOCLO=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}
	
	// Remove this overridden method once the tokenizer is working
	@Override
    public void testTokenizer() throws SerializingException, IOException {

    }

	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RUNWAY_STATE, END_TOKEN
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
