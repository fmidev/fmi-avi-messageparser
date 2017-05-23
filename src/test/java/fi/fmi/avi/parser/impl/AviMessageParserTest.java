package fi.fmi.avi.parser.impl;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingResult;
import fi.fmi.avi.parser.impl.conf.AviMessageParserConfig;

/**
 * Created by rinne on 24/02/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AviMessageParserConfig.class, loader = AnnotationConfigContextLoader.class)
public class AviMessageParserTest extends AviMessageTestBase {

    @Autowired
    private AviMessageLexer lexer;

    @Autowired
    private AviMessageParser parser;

    @Test
    public void testMetar1() throws Exception {
        ParsingResult<Metar> result = parser.parseMessage(lexer.lexMessage(metar1), Metar.class);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertMetarEquals(readFromJSON("metar/metar1.json", MetarImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testMetar2() throws Exception {
        ParsingResult<Metar> result = parser.parseMessage(lexer.lexMessage(metar2), Metar.class);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertMetarEquals(readFromJSON("metar/metar2.json", MetarImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testMetar3() throws Exception {
        // Note: VariableSurfaceWind seems to be missing in the Metar POJO model. Might cause interesting issues when serializing back to a string
        ParsingResult<Metar> result = parser.parseMessage(lexer.lexMessage(metar3), Metar.class);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertMetarEquals(readFromJSON("metar/metar3.json", MetarImpl.class), result.getParsedMessage());
    }

    @Test
    public void testTaf1() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf1, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf1.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf2() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf2, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf2.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf3() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf3, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf3.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf4() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf4, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf4.json", TAFImpl.class), result.getParsedMessage());
    }

    @Test
    public void testTaf5() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf5, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf5.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf6() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf6, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf6.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf7() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf7, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf7.json", TAFImpl.class), result.getParsedMessage());
    }

    @Test
    public void testTaf8() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf8, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf8.json", TAFImpl.class), result.getParsedMessage());
    }

    @Test
    public void testTaf9() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf9, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf9.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf10() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf10, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf10.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf11() throws Exception {
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf11, ParsingHints.TAF), TAF.class, ParsingHints.TAF);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf11.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf12() throws Exception {
        ParsingHints hints = new ParsingHints();
        hints.put(ParsingHints.KEY_MESSAGE_TYPE, ParsingHints.VALUE_MESSAGE_TYPE_TAF);
        hints.put(ParsingHints.KEY_TIMEZONE_ID_POLICY, ParsingHints.VALUE_TIMEZONE_ID_POLICY_STRICT);
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf12, hints), TAF.class, hints);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf12.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf13() throws Exception {
        ParsingHints hints = new ParsingHints();
        hints.put(ParsingHints.KEY_MESSAGE_TYPE, ParsingHints.VALUE_MESSAGE_TYPE_TAF);
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf13, hints), TAF.class, hints);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf13.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf14() throws Exception {
        ParsingHints hints = new ParsingHints();
        hints.put(ParsingHints.KEY_MESSAGE_TYPE, ParsingHints.VALUE_MESSAGE_TYPE_TAF);
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf14, hints), TAF.class, hints);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf14.json", TAFImpl.class), result.getParsedMessage());
    }

    @Test
    public void testTaf15() throws Exception {
        ParsingHints hints = new ParsingHints();
        hints.put(ParsingHints.KEY_MESSAGE_TYPE, ParsingHints.VALUE_MESSAGE_TYPE_TAF);
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf15, hints), TAF.class, hints);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf15.json", TAFImpl.class), result.getParsedMessage());
    }
    
    @Test
    public void testTaf16() throws Exception {
        ParsingHints hints = new ParsingHints();
        hints.put(ParsingHints.KEY_MESSAGE_TYPE, ParsingHints.VALUE_MESSAGE_TYPE_TAF);
        ParsingResult<TAF> result = parser.parseMessage(lexer.lexMessage(taf16, hints), TAF.class, hints);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), ParsingResult.ParsingStatus.SUCCESS, result.getStatus());
        assertTAFEquals(readFromJSON("taf/taf16.json", TAFImpl.class), result.getParsedMessage());
    }
}
