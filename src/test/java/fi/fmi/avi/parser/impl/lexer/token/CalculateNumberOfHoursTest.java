package fi.fmi.avi.parser.impl.lexer.token;

import static org.junit.Assert.*;

import org.junit.Test;

public class CalculateNumberOfHoursTest {

	@Test
	public void testSingleDay() {
		int hours = TAFTimePeriod.calculateNumberOfHours(1, 0, 1, 6);
		assertEquals(6, hours);
	}

	@Test
	public void testNoHours() {
		int hours = TAFTimePeriod.calculateNumberOfHours(1, 0, 1, 0);
		assertEquals(0, hours);
	}

	@Test
	public void testSpanDaySingleHour() {
		int hours = TAFTimePeriod.calculateNumberOfHours(8, 23, 9, 0);
		assertEquals(1, hours);
	}

	@Test
	public void testSpanDayMoreThan24Hours() {
		int hours = TAFTimePeriod.calculateNumberOfHours(8, 6, 9, 12);
		assertEquals(30, hours);
	}
	
	@Test
	public void testSpanMonth() {
		int hours = TAFTimePeriod.calculateNumberOfHours(31, 22, 1, 8);
		assertEquals(10, hours);
	}
	
}
