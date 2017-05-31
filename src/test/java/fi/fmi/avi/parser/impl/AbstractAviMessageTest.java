package fi.fmi.avi.parser.impl;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.ReflectionComparatorFactory;
import org.unitils.reflectionassert.comparator.Comparator;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.impl.conf.AviMessageParserConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AviMessageParserConfig.class, loader = AnnotationConfigContextLoader.class)
public abstract class AbstractAviMessageTest {

	private static final double FLOAT_EQUIVALENCE_THRESHOLD = 0.0000000001d;
	
    @Autowired
    private AviMessageLexer lexer;

    @Autowired
    private AviMessageTACTokenizer tokenizer;

    @Autowired
    private AviMessageParser parser;

    
    public abstract String getMessage();
    public abstract String getTokenizedMessagePrefix();
    public abstract String getJsonFilename();
    
    public abstract Class<? extends AviationWeatherMessage> getMessageClass();
    
    public ParsingHints getLexerParsingHints() {
    	return new ParsingHints();
    }
    
    public ParsingHints getParserParsingHints() {
    	return new ParsingHints();
    }
    
    public abstract Identity[] getLexerTokenSequenceIdentity();
    
    public ParsingHints getTokenizerParsingHints() {
    	ParsingHints hints = new ParsingHints(ParsingHints.KEY_VALIDTIME_FORMAT, ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_LONG);
    	return hints;
    }
    
	@Test
	public void testLexer() {
		LexemeSequence result = lexer.lexMessage(getMessage(), getLexerParsingHints());
        assertTokenSequenceIdentityMatch(result, getLexerTokenSequenceIdentity());
	}
	
	@Test
	public void testTokenizer() throws TokenizingException, IOException {
		String expectedMessage = getTokenizedMessagePrefix() + getMessage();
		
        assertTokenSequenceMatch(
        		expectedMessage,
        		getJsonFilename(),
        		getMessageClass(),
        		getTokenizerParsingHints());
	}

	public ParsingResult.ParsingStatus getExpectedParsingStatus() {
		return ParsingResult.ParsingStatus.SUCCESS;
	}
	
	// Override when necessary
	public void assertParsingIssues(List<ParsingIssue> parsingIssues) {
		assertEquals("No parsing issues expected", 0, parsingIssues.size());
	}
	
	@Test
	public void testParser() throws IOException {
		LexemeSequence lexemeSequence = lexer.lexMessage(getMessage(), getLexerParsingHints());
		Class<? extends AviationWeatherMessage> clazz = getMessageClass();
		ParsingHints hints = getParserParsingHints();
		
        ParsingResult<? extends AviationWeatherMessage> result = parser.parseMessage(lexemeSequence, clazz, hints);
        assertEquals("Parsing was not successful: " + result.getParsingIssues(), getExpectedParsingStatus(), result.getStatus());
        assertParsingIssues(result.getParsingIssues());
        assertAviationWeatherMessageEquals(readFromJSON(getJsonFilename(), getMessageClass()), result.getParsedMessage());
	}
	

    protected void assertTokenSequenceIdentityMatch(LexemeSequence result, Lexeme.Identity... identities) {
		List<Lexeme> lexemes = result.getLexemes();
		assertTrue("Token sequence size does not match", identities.length == lexemes.size());
		for (int i = 0; i < identities.length; i++) {
			assertEquals("Mismatch at index " + i, identities[i], lexemes.get(i).getIdentityIfAcceptable());
		}
	}
    
    protected void assertTokenSequenceMatch(final String expected, final String fileName, Class<? extends AviationWeatherMessage> clz, final ParsingHints hints) throws IOException, TokenizingException {
        LexemeSequence seq = tokenizer.tokenizeMessage(readFromJSON(fileName, clz), hints);
        assertNotNull("Null sequence was produced", seq);
        assertEquals(expected, seq.getTAC());
    }
    
    protected <T extends AviationWeatherMessage> T readFromJSON(String fileName, Class<T> clz) throws IOException {
        T retval = null;
        ObjectMapper om = new ObjectMapper();
        InputStream is = AbstractAviMessageTest.class.getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            retval = om.readValue(is, clz);
        } else {
            throw new FileNotFoundException("Resource '" + fileName + "' could not be loaded");
        }
        return retval;
    }
    
    protected static void assertAviationWeatherMessageEquals(AviationWeatherMessage expected, AviationWeatherMessage actual) {
    	Difference diff = deepCompareObjects(expected, actual);
    	if (diff != null) {
            StringBuilder failureMessage = new StringBuilder();
            failureMessage.append("AviationWeatherMessage objects are not equivalent\n");
            failureMessage.append(new DefaultDifferenceReport().createReport(diff));
            fail(failureMessage.toString());
    	}
    }
    
    protected static Difference deepCompareObjects(Object expected, Object actual) {
    	
    	// Use anonymous class to call protected member function
    	LinkedList<Comparator> comparatorChain = (new ReflectionComparatorFactory() {
	    		LinkedList<Comparator> createBaseComparators() {
	    			return new LinkedList<Comparator>(getComparatorChain(Collections.emptySet()));
	    		}
	    	}).createBaseComparators();
    	
    	// Add lenient collection comparator ([] == null) as first-in-chain
    	comparatorChain.addFirst(new Comparator() {
			
			@Override
			public Difference compare(Object left, Object right, boolean onlyFirstDifference,
					ReflectionComparator reflectionComparator) {
				Collection<?> coll = (Collection<?>)left;
				if (coll == null) {
					coll = (Collection<?>)right;
				}
				
				if (coll.size() == 0) {
					return null;
				}
				
				return new Difference("Null list does not match a non-empty list", left, right);
			}
			
			@Override
			public boolean canCompare(Object left, Object right) {
				return
					(left == null && right instanceof Collection<?>) || 
					(right == null && left instanceof Collection<?>);
			}
		});
    	
    	// Add double comparator with specified accuracy as first-in-chain
    	comparatorChain.addFirst(new Comparator() {
    		@Override
    		public Difference compare(Object left, Object right, boolean onlyFirstDifference,
    				ReflectionComparator reflectionComparator) {
    			double diff = Math.abs( ((Double)left) - ((Double)right));
    			if (diff >= FLOAT_EQUIVALENCE_THRESHOLD) {
    				return new Difference("Floating point values differ more than set threshold", left, right);
    			}
    			
    			return null;
    		}
    		
    		@Override
    		public boolean canCompare(Object left, Object right) {
    			return left instanceof Double && right instanceof Double;
    		}
    	
    	});
    	
    	
    	ReflectionComparator reflectionComparator = new ReflectionComparator(comparatorChain);
    	return reflectionComparator.getDifference(expected, actual);
    }
}
