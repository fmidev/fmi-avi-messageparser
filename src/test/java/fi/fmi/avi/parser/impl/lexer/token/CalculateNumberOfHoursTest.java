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
	public void test24HourIllegalButUsedFormat() {
		int hours = TAFTimePeriod.calculateNumberOfHours(8, 0, 8, 24);
		assertEquals(24, hours);
	}


	@Test
	public void testSpanDayMoreThan24Hours() {
		int hours = TAFTimePeriod.calculateNumberOfHours(8, 6, 9, 12);
		assertEquals(30, hours);
	}
	
	@Test
	public void testSpanToNextMonthStartHoursMoreThanEndHours() {
		int hours = TAFTimePeriod.calculateNumberOfHours(31, 22, 1, 8);
		assertEquals(10, hours);
	}

	
	@Test
	public void testSpanToNextMonthStartHoursLessThanEndHours_starts31st() {
		int hours = TAFTimePeriod.calculateNumberOfHours(31, 8, 1, 22);
		assertEquals(38, hours);
	}
	
	@Test
	public void testSpanToNextMonthStartHoursLessThanEndHours_starts30th() {
		int hours = TAFTimePeriod.calculateNumberOfHours(30, 8, 1, 22);
		assertEquals(38, hours);
	}
	
	@Test
	public void testSpanToNextMonthStartHoursLessThanEndHours_starts29th() {
		int hours = TAFTimePeriod.calculateNumberOfHours(29, 8, 1, 22);
		assertEquals(38, hours);
	}
	
	@Test
	public void testSpanToNextMonthStartHoursLessThanEndHours_starts28th() {
		int hours = TAFTimePeriod.calculateNumberOfHours(28, 8, 1, 22);
		assertEquals(38, hours);
	}
	

	@Test
	public void testSpanToNextMonthStartHoursLessThanEndHours_starts27th_illegal() {
		try {
			int hours = TAFTimePeriod.calculateNumberOfHours(27, 8, 1, 22);
			fail("hours should not have been calculated "+hours);
		} catch(Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}
	
	@Test
	public void testIllegalSpanTooLong() {
		try {
			int hours = TAFTimePeriod.calculateNumberOfHours(15, 22, 1, 8);
			fail("hours should not have been calculated "+hours);
		} catch(Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}
	
}
