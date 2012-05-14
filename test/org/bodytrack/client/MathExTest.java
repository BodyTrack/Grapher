package org.bodytrack.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

// This class can safely use the built-in Java math methods to test
// the MathEx implementations because tests run during the build and don't
// depend on GWT's libraries
public class MathExTest {
	private static final long SEED = 424242;
	private static final int NTESTVALUES = 10000;

	private static final long SIGN_BIT = 1L << 63;

	@Test
	public void testToLongBitsZero() {
		checkDoubleToLongBits(0.0, 0L);
		checkDoubleToLongBits(-0.0, SIGN_BIT);
	}

	@Test
	public void testToLongBitsInfinity() {
		checkDoubleToLongBits(Double.POSITIVE_INFINITY, 0x7ff0000000000000L);
		checkDoubleToLongBits(Double.NEGATIVE_INFINITY, 0xfff0000000000000L);
	}

	@Test
	public void testToLongBitsNaN() {
		checkDoubleToLongBits(Double.NaN, 0x7ff8000000000000L);
	}

	@Test
	public void testToLongBitsEdgeValues() {
		checkDoubleToLongBits(Double.MAX_VALUE, 0x7fefffffffffffffL);
		checkDoubleToLongBits(Double.MIN_VALUE, 0x1L);
		checkDoubleToLongBits(Double.MIN_NORMAL, 0x0010000000000000L);

		checkDoubleToLongBits(-Double.MAX_VALUE, SIGN_BIT | 0x7fefffffffffffffL);
		checkDoubleToLongBits(-Double.MIN_VALUE, SIGN_BIT | 0x1L);
		checkDoubleToLongBits(-Double.MIN_NORMAL, SIGN_BIT | 0x0010000000000000L);
	}

	@Test
	public void testToLongBitsRandomZeroOne() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			final double d = rg.nextDouble();
			checkDoubleToLongBits(d);
			checkDoubleToLongBits(-d);
		}
	}

	@Test
	public void testToLongBitsRandomNormalized() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			double d = 0.0;
			while (d < Double.MIN_NORMAL)
				d = rg.nextDouble() * Double.MAX_VALUE;

			checkDoubleToLongBits(d);
			checkDoubleToLongBits(-d);
		}
	}

	@Test
	public void testToLongBitsRandomDenormalized() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			final double d = rg.nextDouble() * Double.MIN_NORMAL;

			checkDoubleToLongBits(d);
			checkDoubleToLongBits(-d);
		}
	}

	private void checkDoubleToLongBits(final double d, final long correct) {
		checkDoubleToLongBits(d);
		assertEquals(correct, MathEx.doubleToLongBits(d));
	}

	private void checkDoubleToLongBits(final double d) {
		assertEquals(Double.doubleToLongBits(d),
				MathEx.doubleToLongBits(d));
	}

	@Test
	public void testToDoubleZero() {
		checkLongBitsToDouble(0L, 0.0);
		checkLongBitsToDouble(SIGN_BIT, -0.0);
	}

	@Test
	public void testToDoubleInfinity() {
		checkLongBitsToDouble(0x7ff0000000000000L, Double.POSITIVE_INFINITY);
		checkLongBitsToDouble(0xfff0000000000000L, Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testToDoubleNaN() {
		checkLongBitsToDouble(0x7ff8000000000000L, Double.NaN);
	}

	@Test
	public void testToDoubleEdgeValues() {
		checkLongBitsToDouble(0x7fefffffffffffffL, Double.MAX_VALUE);
		checkLongBitsToDouble(0x1L, Double.MIN_VALUE);
		checkLongBitsToDouble(0x0010000000000000L, Double.MIN_NORMAL);

		checkLongBitsToDouble(SIGN_BIT | 0x7fefffffffffffffL, -Double.MAX_VALUE);
		checkLongBitsToDouble(SIGN_BIT | 0x1L, -Double.MIN_VALUE);
		checkLongBitsToDouble(SIGN_BIT | 0x0010000000000000L, -Double.MIN_NORMAL);
	}

	@Test
	public void testToDoubleRandomDoubleZeroOne() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			final double d = rg.nextDouble();

			checkLongBitsToDouble(Double.doubleToLongBits(d), d);
			checkLongBitsToDouble(Double.doubleToLongBits(-d), -d);
		}
	}

	@Test
	public void testToDoubleRandomDoubleNormalized() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			double d = 0.0;
			while (d < Double.MIN_NORMAL)
				d = rg.nextDouble() * Double.MAX_VALUE;

			checkLongBitsToDouble(Double.doubleToLongBits(d), d);
			checkLongBitsToDouble(Double.doubleToLongBits(-d), -d);
		}
	}

	@Test
	public void testToDoubleRandomDoubleDenormalized() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			final double d = rg.nextDouble() * Double.MIN_NORMAL;

			checkLongBitsToDouble(Double.doubleToLongBits(d), d);
			checkLongBitsToDouble(Double.doubleToLongBits(-d), -d);
		}
	}

	@Test
	public void testToDoubleRandomLong() {
		Random rg = new Random(SEED);

		for (int i = 0; i < NTESTVALUES; i++) {
			checkLongBitsToDouble(rg.nextLong());
		}
	}

	private void checkLongBitsToDouble(final long l, final double correct) {
		checkLongBitsToDouble(l);
		assertTrue(Double.valueOf(correct).equals(MathEx.longBitsToDouble(l)));
	}

	private void checkLongBitsToDouble(final long l) {
		assertTrue(Double.valueOf(Double.longBitsToDouble(l)).equals(
				MathEx.longBitsToDouble(l)));
	}
}
