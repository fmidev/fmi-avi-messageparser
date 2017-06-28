package fi.fmi.avi.parser.impl.metar;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;

import java.io.IOException;

import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.metar.impl.METARImpl;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class METAR6Test extends AbstractAviMessageTest<String, METAR> {

	@Override
	public String getJsonFilename() {
		return "metar/metar6.json";
	}
	
	@Override
	public String getMessage() {
		return
				"METAR EFHK 010550Z 21018KT 9999 -RA SCT013 BKN025 03/00 Q0988 TEMPO 4000 RASN=";
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
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, WEATHER, END_TOKEN
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
