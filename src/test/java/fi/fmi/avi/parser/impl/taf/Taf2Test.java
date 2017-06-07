package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;

import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Taf2Test extends AbstractAviMessageTest<String, TAF> {

	@Override
	public String getJsonFilename() {
		return "taf/taf2.json";
	}
	
	@Override
	public String getMessage() {
		return
				"TAF EFAB 190815Z 1909/1915 14008G15MPS 9999 BKN010 BKN015=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}

	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CLOUD, END_TOKEN };
	}

    @Override
    public ConversionSpecification<String, TAF> getParserSpecification() {
        return ConversionSpecification.TAC_TO_TAF;
    }

    @Override
    public Class<? extends TAF> getTokenizerImplmentationClass() {
        return TAFImpl.class;
    }

}
