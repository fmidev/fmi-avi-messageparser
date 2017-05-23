package fi.fmi.avi.parser.impl.taf;

import static fi.fmi.avi.parser.Lexeme.Identity.*;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Taf4Test extends AbstractAviMessageTest {

	@Override
	public String getJsonFilename() {
		return "taf/taf4.json";
	}
	
	@Override
	public String getMessage() {
		return
				"TAF AMD EFAB 191100Z 1909/1915 CNL=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "";
	}
	
	@Override
	public ParsingHints getLexerParsingHints() {
		return ParsingHints.TAF;
	}
	
	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				TAF_START, AMENDMENT, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, CANCELLATION, END_TOKEN
		};
	}
	
	@Override
	public Class<? extends AviationWeatherMessage> getMessageClass() {
		return TAFImpl.class;
	}

}
