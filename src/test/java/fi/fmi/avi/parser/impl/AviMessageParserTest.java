package fi.fmi.avi.parser.impl;

import org.junit.Ignore;
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
        assertMetarEquals(readFromJSON("metar/metar1.json", MetarImpl.class), parser.parseMessage(lexer.lexMessage(metar1), Metar.class));
    }
    
    @Test
    public void testMetar2() throws Exception {
        assertMetarEquals(readFromJSON("metar/metar2.json", MetarImpl.class), parser.parseMessage(lexer.lexMessage(metar2), Metar.class));
    }

    @Test
    @Ignore
    public void testTaf1() throws Exception {
        assertTAFEquals(readFromJSON("taf/taf1.json", TAFImpl.class),
                parser.parseMessage(lexer.lexMessage(taf1, ParsingHints.TAF), TAF.class, ParsingHints.TAF));
    }
}
