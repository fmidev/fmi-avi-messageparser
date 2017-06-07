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
import static fi.fmi.avi.parser.Lexeme.Identity.VARIABLE_WIND_DIRECTION;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult.ParsingStatus;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.AbstractAviMessageTest;

public class Metar15Test extends AbstractAviMessageTest<String, Metar> {

	@Override
	public String getJsonFilename() {
		return "metar/metar15.json";
	}
	
	@Override
	public String getMessage() {
		return
				"EFKK 091050Z AUTO 01009KT 340V040 9999 FEW012 BKN046 ///// Q////=";
	}
	
	@Override
	public String getTokenizedMessagePrefix() {
		return "METAR ";
	}
	
	@Override
    public ConversionHints getLexerParsingHints() {
        return ConversionHints.METAR;
    }

    @Override
    public ConversionHints getParserParsingHints() {
        return ConversionHints.METAR;
    }

	// Remove this overridden method once the tokenizer is working
	@Override
    public void testTokenizer() throws SerializingException, IOException {

    }

	@Override
    public ParsingStatus getExpectedParsingStatus() {
        return ParsingStatus.WITH_ERRORS;
    }

    @Override
    public void assertParsingIssues(List<ParsingIssue> parsingIssues) {
        assertEquals(2, parsingIssues.size());

        ParsingIssue issue = parsingIssues.get(0);
        assertEquals(ParsingIssue.Type.MISSING_DATA, issue.getType());
        assertEquals("Missing air temperature and dewpoint temperature values in /////", issue.getMessage());

        issue = parsingIssues.get(1);
        assertEquals(ParsingIssue.Type.MISSING_DATA, issue.getType());
        assertEquals("Missing air pressure value: Q////", issue.getMessage());

    }

	@Override
	public Identity[] getLexerTokenSequenceIdentity() {
		return new Identity[] {
				METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, VARIABLE_WIND_DIRECTION,
                HORIZONTAL_VISIBILITY, CLOUD, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, END_TOKEN
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
