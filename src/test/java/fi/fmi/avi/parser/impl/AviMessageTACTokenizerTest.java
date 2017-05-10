package fi.fmi.avi.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.conf.AviMessageParserConfig;

/**
 * Created by rinne on 23/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AviMessageParserConfig.class, loader = AnnotationConfigContextLoader.class)
public class AviMessageTACTokenizerTest extends AviMessageTestBase {

    @Autowired
    private AviMessageTACTokenizer tokenizer;

    @Test
    @Ignore
    public void testMetar1() throws Exception {
        assertTokenSequenceMatch(metar1, "metar/metar1.json", MetarImpl.class, null);
    }

    @Test
    public void testTAF1() throws Exception {
    	ParsingHints hints = new ParsingHints(ParsingHints.KEY_VALIDTIME_FORMAT, ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_SHORT);
        assertTokenSequenceMatch("TAF " + taf1, "taf/taf1.json", TAFImpl.class, hints);
    }

    private void assertTokenSequenceMatch(final String expected, final String fileName, Class<? extends AviationWeatherMessage> clz, final ParsingHints hints) throws IOException, TokenizingException {
        LexemeSequence seq = tokenizer.tokenizeMessage(readFromJSON(fileName, clz), hints);
        assertNotNull("Null sequence was produced", seq);
        assertEquals(expected, seq.getTAC());
    }

}
