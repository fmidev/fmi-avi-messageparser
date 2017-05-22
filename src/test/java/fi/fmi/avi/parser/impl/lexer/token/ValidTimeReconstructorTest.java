package fi.fmi.avi.parser.impl.lexer.token;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.TokenizingException;
import fi.fmi.avi.parser.Lexeme.Identity;
import fi.fmi.avi.parser.impl.lexer.LexingFactoryImpl;
import fi.fmi.avi.parser.impl.lexer.TACTokenReconstructor;

public class ValidTimeReconstructorTest {

	TACTokenReconstructor reconstructor;
	ParsingHints hints;
	
	@Before
	public void setUp() throws Exception {
		reconstructor = new ValidTime.Reconstructor();
		reconstructor.setLexingFactory(new LexingFactoryImpl());
		hints = new ParsingHints();
	}

	@Test
	public void testValidityLongDateFormat() throws TokenizingException
	{
		TAF msg = mock(TAF.class);
		
		injectValidity(msg, 7, 2, 7, 24);
		
		List<Lexeme> l = reconstructor.getAsLexemes(msg, TAF.class, hints);
		
		assertOneLexeme(l, Lexeme.Identity.VALID_TIME, "0702/0724");
	}

	@Test
	public void testValidityShortDateFormat() throws TokenizingException
	{
		TAF msg = mock(TAF.class);
		
		hints.put(ParsingHints.KEY_VALIDTIME_FORMAT, ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_SHORT);
		injectValidity(msg, 7, 2, 7, 24);
		
		
		List<Lexeme> l = reconstructor.getAsLexemes(msg, TAF.class, hints);
		
		assertOneLexeme(l, Lexeme.Identity.VALID_TIME, "070224");
	}


	@Test
	public void testValidityShortDateFormatNextDay() throws TokenizingException
	{
		TAF msg = mock(TAF.class);
		
		hints.put(ParsingHints.KEY_VALIDTIME_FORMAT, ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_SHORT);
		injectValidity(msg, 7, 18, 8, 10);
		
		
		List<Lexeme> l = reconstructor.getAsLexemes(msg, TAF.class, hints);
		
		assertOneLexeme(l, Lexeme.Identity.VALID_TIME, "071810");
	}


	@Test
	public void testValidityShortDateFormatTooLongPeriod() throws TokenizingException
	{
		TAF msg = mock(TAF.class);
		
		hints.put(ParsingHints.KEY_VALIDTIME_FORMAT, ParsingHints.VALUE_VALIDTIME_FORMAT_PREFER_SHORT);
		injectValidity(msg, 7, 18, 8, 20);
		
		
		List<Lexeme> l = reconstructor.getAsLexemes(msg, TAF.class, hints);
		
		assertOneLexeme(l, Lexeme.Identity.VALID_TIME, "0718/0820");
	}

	private void injectValidity(TAF msg, int startDay, int startHour, int endDay, int endHour) {
		when(msg.getValidityStartDayOfMonth()).thenReturn(startDay);
		when(msg.getValidityStartHour()).thenReturn(startHour);
		
		when(msg.getValidityEndDayOfMonth()).thenReturn(endDay);
		when(msg.getValidityEndHour()).thenReturn(endHour);
	}

	private void assertOneLexeme(List<Lexeme> lexemes, Identity identity, String token) {
		assertNotNull(lexemes);
		assertEquals(1, lexemes.size());
		assertLexeme(lexemes.get(0), identity, token);
	}
	
	private void assertLexeme(Lexeme l, Identity identity, String token) {
		assertNotNull(l);
		assertEquals(identity, l.getIdentity());
		assertEquals(token, l.getTACToken());
		
	}

}
