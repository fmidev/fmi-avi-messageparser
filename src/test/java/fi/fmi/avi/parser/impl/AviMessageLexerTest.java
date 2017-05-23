package fi.fmi.avi.parser.impl;

import static fi.fmi.avi.parser.Lexeme.Identity.AERODROME_DESIGNATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.AIR_PRESSURE_QNH;
import static fi.fmi.avi.parser.Lexeme.Identity.AMENDMENT;
import static fi.fmi.avi.parser.Lexeme.Identity.AUTOMATED;
import static fi.fmi.avi.parser.Lexeme.Identity.CANCELLATION;
import static fi.fmi.avi.parser.Lexeme.Identity.CAVOK;
import static fi.fmi.avi.parser.Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP;
import static fi.fmi.avi.parser.Lexeme.Identity.CLOUD;
import static fi.fmi.avi.parser.Lexeme.Identity.COLOR_CODE;
import static fi.fmi.avi.parser.Lexeme.Identity.CORRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.END_TOKEN;
import static fi.fmi.avi.parser.Lexeme.Identity.FORECAST_CHANGE_INDICATOR;
import static fi.fmi.avi.parser.Lexeme.Identity.HORIZONTAL_VISIBILITY;
import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.MAX_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.MIN_TEMPERATURE;
import static fi.fmi.avi.parser.Lexeme.Identity.NIL;
import static fi.fmi.avi.parser.Lexeme.Identity.NO_SIGNIFICANT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.RECENT_WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARK;
import static fi.fmi.avi.parser.Lexeme.Identity.REMARKS_START;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_VISUAL_RANGE;
import static fi.fmi.avi.parser.Lexeme.Identity.SEA_STATE;
import static fi.fmi.avi.parser.Lexeme.Identity.SURFACE_WIND;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;
import static fi.fmi.avi.parser.Lexeme.Identity.VALID_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.VARIABLE_WIND_DIRECTION;
import static fi.fmi.avi.parser.Lexeme.Identity.WEATHER;
import static fi.fmi.avi.parser.Lexeme.Identity.WIND_SHEAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.conf.AviMessageParserConfig;

/**
 * Created by rinne on 23/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AviMessageParserConfig.class, loader = AnnotationConfigContextLoader.class)
public class AviMessageLexerTest extends AviMessageTestBase {

    @Autowired
    private AviMessageLexer lexer;

    /**************
     * METAR tests
     **************/

    @Test
    public void testMetar1() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar1);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, END_TOKEN);
    }

    @Test
    public void testMetar2() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar2);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, WEATHER, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, REMARKS_START, REMARK, REMARK, REMARK, REMARK, END_TOKEN);
    }

    @Test
    public void testMetar3() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar3);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, VARIABLE_WIND_DIRECTION, HORIZONTAL_VISIBILITY,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RUNWAY_STATE,
                FORECAST_CHANGE_INDICATOR, END_TOKEN);
    }

    @Test
    public void testMetar4() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar4);
        assertTokenSequenceIdentityMatch(result, METAR_START, CORRECTION, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, VARIABLE_WIND_DIRECTION,
                HORIZONTAL_VISIBILITY, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER,
                CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RECENT_WEATHER, FORECAST_CHANGE_INDICATOR,
                HORIZONTAL_VISIBILITY, END_TOKEN);
    }

    @Test
    public void testMetar5() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar5);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, CAVOK, AIR_DEWPOINT_TEMPERATURE,
                AIR_PRESSURE_QNH, END_TOKEN);
    }

    @Test
    public void testMetar6() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar6);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, WEATHER, END_TOKEN);
    }

    @Test
    public void testMetar7() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar7, ParsingHints.METAR);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, COLOR_CODE, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, COLOR_CODE,
                END_TOKEN);
    }

    @Test
    public void testMetar8() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar8, ParsingHints.METAR);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, COLOR_CODE, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, COLOR_CODE,
                END_TOKEN);
    }

    @Test
    public void testMetar9() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar9);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                RECENT_WEATHER, WIND_SHEAR, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, END_TOKEN);
    }

    @Test
    public void testMetar10() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar10);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                RECENT_WEATHER, WIND_SHEAR, FORECAST_CHANGE_INDICATOR, HORIZONTAL_VISIBILITY, END_TOKEN);
    }

    @Test
    public void testMetar11() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar11);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH,
                SEA_STATE, FORECAST_CHANGE_INDICATOR, FORECAST_CHANGE_INDICATOR, WEATHER, CLOUD, END_TOKEN);
    }

    @Test
    public void testMetar12() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar12);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RUNWAY_STATE,
                RUNWAY_STATE, END_TOKEN);
    }

    @Test
    public void testMetar13() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar13);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, RUNWAY_VISUAL_RANGE,
                RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, RUNWAY_VISUAL_RANGE, WEATHER, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RUNWAY_STATE,
                END_TOKEN);
    }

    @Test
    public void testMetar14() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar14);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, RUNWAY_STATE, END_TOKEN);
    }

    @Test
    public void testMetar15() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar15, ParsingHints.METAR);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, VARIABLE_WIND_DIRECTION,
                HORIZONTAL_VISIBILITY, CLOUD, CLOUD, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, END_TOKEN);
    }
    
    @Test
    public void testMetar16() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar16, ParsingHints.METAR);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, AUTOMATED, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, AIR_DEWPOINT_TEMPERATURE,
                AIR_PRESSURE_QNH, END_TOKEN);
    }

    @Test
    public void testMetar17() throws Exception {
        LexemeSequence result = lexer.lexMessage(metar17);
        assertTokenSequenceIdentityMatch(result, METAR_START, AERODROME_DESIGNATOR, ISSUE_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, WEATHER, CLOUD,
                AIR_DEWPOINT_TEMPERATURE, AIR_DEWPOINT_TEMPERATURE, AIR_PRESSURE_QNH, END_TOKEN);
    }
    
    @Test
    public void testNonMetar() throws Exception {
        LexemeSequence result = lexer.lexMessage("FOO BAR 5000 30025KT=");
        Lexeme.Identity[] recognized = new Lexeme.Identity[]{null, null, null, null, END_TOKEN}; 
        assertTokenSequenceIdentityMatch(result, recognized);
    }

    /**************
     * TAF tests
     **************/

    @Test
    public void testTAF1() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf1, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD,
                CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP,
                HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN);
    }

    @Test
    public void testTAF2() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf2);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CLOUD,
                END_TOKEN);
    }

    @Test
    public void testTAF3() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf3);
        assertTokenSequenceIdentityMatch(result, TAF_START, AMENDMENT, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, CAVOK, END_TOKEN);
    }

    @Test
    public void testTAF4() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf4);
        assertTokenSequenceIdentityMatch(result, TAF_START, AMENDMENT, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, CANCELLATION, END_TOKEN);
    }

    @Test
    public void testTAF5() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf5, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD,
                CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, END_TOKEN);
    }

    @Test
    public void testTAF6() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf6);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, CAVOK, FORECAST_CHANGE_INDICATOR,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, END_TOKEN);
    }

    @Test
    public void testTAF7() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf7);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, NIL, END_TOKEN);
    }

    @Test
    public void testTAF8() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf8, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD,
                CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP,
                HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN);
    }

    @Test
    public void testTAF9() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf9, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP,
                SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN);
    }

    @Test
    public void testTAF10() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf10, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY,
                WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN);
    }

    @Test
    public void testTAF11() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf11, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, FORECAST_CHANGE_INDICATOR,
                SURFACE_WIND, HORIZONTAL_VISIBILITY, NO_SIGNIFICANT_WEATHER, CLOUD, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND,
                HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD, END_TOKEN);
    }

    @Test
    public void testTAF12() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf12, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD, CLOUD,
                MAX_TEMPERATURE, MIN_TEMPERATURE, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER, WEATHER, CLOUD,
                FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, CLOUD, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP,
                HORIZONTAL_VISIBILITY, WEATHER, CLOUD, CLOUD, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, END_TOKEN);
    }
    
    @Test
    public void testTAF13() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf13, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, VALID_TIME, SURFACE_WIND, HORIZONTAL_VISIBILITY, WEATHER, CLOUD, 
        		FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER,
        		FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER,
        		FORECAST_CHANGE_INDICATOR, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, WEATHER,
        		FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, SURFACE_WIND, HORIZONTAL_VISIBILITY, CLOUD,
        		FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, HORIZONTAL_VISIBILITY, CLOUD,
        		END_TOKEN);
    }
    
    @Test
    public void testTAF14() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf14, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, 
        		VALID_TIME, SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, REMARKS_START, REMARK, REMARK,
        		REMARK, REMARK, REMARK, END_TOKEN);
    }

    @Test
    public void testTAF15() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf15, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME,
        		VALID_TIME, SURFACE_WIND, CAVOK, FORECAST_CHANGE_INDICATOR, CHANGE_FORECAST_TIME_GROUP, 
        		HORIZONTAL_VISIBILITY, WEATHER, CLOUD, END_TOKEN);
    }

    @Test
    public void testTAF16() throws Exception {
        LexemeSequence result = lexer.lexMessage(taf16, ParsingHints.TAF);
        assertTokenSequenceIdentityMatch(result, TAF_START, AERODROME_DESIGNATOR, ISSUE_TIME, 
        		VALID_TIME, SURFACE_WIND, CAVOK, HORIZONTAL_VISIBILITY, REMARKS_START, REMARK, REMARK,
        		REMARK, REMARK, REMARK, END_TOKEN);
    }
    
    private void assertTokenSequenceIdentityMatch(LexemeSequence result, Lexeme.Identity... identities) {
        Iterator<Lexeme> it = result.getLexemes();
        if (identities.length > 0) {
            assertTrue(it.hasNext());
        }
        try {
            for (int i = 0; i < identities.length; i++) {
                assertEquals("Mismatch at index " + i, identities[i], it.next().getIdentityIfAcceptable());
            }
        } catch (NoSuchElementException nse) {
            fail("Lexed token sequence is shorter than expected");
        }
        if (it.hasNext()) {
            fail("Lexed token sequence is longer than expected");
        }
    }

}
