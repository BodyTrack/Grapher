package org.bodytrack.client;

/**
 * Provides some math methods that GWT doesn't implement.
 */
public final class MathEx {
	private static final double ULPS_OF_MAX_VALUE = Math.pow(2, 971);

	private static final long SIGN_BIT = 0x8000000000000000L;
	private static final long ZERO_BITS = 0x0L;
	private static final long NEGATIVE_ZERO_BITS = 0x8000000000000000L;
	private static final long POSITIVE_INFINITY_BITS = 0x7ff0000000000000L;
	private static final long NEGATIVE_INFINITY_BITS = 0xfff0000000000000L;
	private static final long NAN_BITS = 0x7ff8000000000000L;
	private static final long MIN_NORMAL_BITS = 0x0010000000000000L;

	private static final int SIGNIFICAND_NBITS = 52;
	private static final long SIGNIFICAND_ALL_ONES = (1L << SIGNIFICAND_NBITS) - 1;
	private static final int EXPONENT_NBITS = 11;
	private static final long EXPONENT_ALL_ONES = (1L << EXPONENT_NBITS) - 1;
	private static final int EXPONENT_BIAS = (1 << (EXPONENT_NBITS - 1)) - 1;

	/** Used to speed up the {@link #log2(double)} method. */
	private static final double LN_2 = Math.log(2);

	private MathEx() {
		throw new UnsupportedOperationException();
	}

	// Modified version of http://stackoverflow.com/a/9487719, covering
	// the special cases of the ulp function as described at
	// http://docs.oracle.com/javase/6/docs/api/java/lang/Math.html
	public static double ulp(final double d) {
		if (Double.isNaN(d))
			return Double.NaN;
		if (Double.isInfinite(d))
			return Double.POSITIVE_INFINITY;
		if (d == 0.0 || d == -0.0)
			return Double.MIN_VALUE;
		if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE)
			return ULPS_OF_MAX_VALUE;

		final long bits = doubleToLongBits(Math.abs(d));
		final double next = longBitsToDouble(bits + 1);
		return next - d;
	}

    public static double nextDouble(final double d) {
        if (Double.isNaN(d))
            return Double.NaN;
        if (Double.isInfinite(d))
            return d;
        if (d == 0.0 || d == -0.0)
            return Double.MIN_VALUE;
        if (d == Double.MAX_VALUE)
            return Double.POSITIVE_INFINITY;

        final long bits = doubleToLongBits(Math.abs(d));
        return longBitsToDouble(bits + 1);
    }

	public static long doubleToLongBits(final double d) {
		final Double dObj = Double.valueOf(d);
		final double absd = Math.abs(d);

		// Need to use .equals rather than == because 0.0 == -0.0 but
		// Double.valueOf(0.0).equals(-0.0) does NOT hold, as is required
		// for a method that produces different bit patterns for 0.0 and -0.0
		if (dObj.equals(0.0))
			return ZERO_BITS;
		if (dObj.equals(-0.0))
			return NEGATIVE_ZERO_BITS;
		if (Double.isInfinite(d))
			return d > 0 ? POSITIVE_INFINITY_BITS : NEGATIVE_INFINITY_BITS;
		if (Double.isNaN(d))
			return NAN_BITS;

		if (absd == Double.MIN_NORMAL)
			return d > 0 ? MIN_NORMAL_BITS : (MIN_NORMAL_BITS | SIGN_BIT);

		if (absd < Double.MIN_NORMAL)
			return doubleToLongBitsDenormalized(d);

		return doubleToLongBitsNormalized(d);
	}

	private static long doubleToLongBitsNormalized(final double d) {
		final double absd = Math.abs(d);

		double significand = absd;
		long exponent = 0;

		// TODO: Better way to compute significand and exponent than a
		// pair of loops?
		while (significand >= 2.0) {
			significand /= 2.0;
			exponent++;
		}

		while (significand < 1.0) {
			significand *= 2.0;
			exponent--;
		}

		final long e = exponent + EXPONENT_BIAS;
		final long m = (long)((significand - 1.0) * Math.pow(2, SIGNIFICAND_NBITS));
		final long result = (e << SIGNIFICAND_NBITS) | m;

		return d > 0 ? result : (result | SIGN_BIT);
	}

	private static long doubleToLongBitsDenormalized(final double d) {
		final double absd = Math.abs(d);

		// Can't use a single Math.pow because otherwise we get a value larger
		// than Double.MAX_VALUE, which messes up the calculations
		final long result = (long)(absd * Math.pow(2, EXPONENT_BIAS - 1)
				* Math.pow(2, SIGNIFICAND_NBITS));

		return d > 0 ? result : (result | SIGN_BIT);
	}

	public static double longBitsToDouble(final long bits) {
		final boolean positive = (bits & SIGN_BIT) == 0;
		final long exponentBits = (bits >> SIGNIFICAND_NBITS) & EXPONENT_ALL_ONES;
		final long significandBits = bits & SIGNIFICAND_ALL_ONES;

		final double sign = positive ? 1.0 : -1.0;

		if (exponentBits == 0)
			return sign * longBitsToDoubleDenormalized(significandBits);

		if (exponentBits == EXPONENT_ALL_ONES) {
			if (significandBits == 0)
				return positive
					? Double.POSITIVE_INFINITY
					: Double.NEGATIVE_INFINITY;

			return Double.NaN;
		}

		return sign * longBitsToDoubleNormalized(exponentBits, significandBits);
	}

	private static double longBitsToDoubleNormalized(final long exponentBits,
			final long significandBits) {
		final int exponent = (int)(exponentBits - EXPONENT_BIAS);
		final double significand =
			(significandBits * Math.pow(2, -SIGNIFICAND_NBITS)) + 1.0;

		return significand * Math.pow(2, exponent);
	}

	private static double longBitsToDoubleDenormalized(final long significandBits) {
		final double significand =
			(significandBits * Math.pow(2, -SIGNIFICAND_NBITS));
		return significand * Math.pow(2, 1 - EXPONENT_BIAS);
	}

	/**
	 * Computes the floor of the log (base 2) of x.
	 *
	 * @param x
	 * 	The value for which we want to take the log
	 * @return
	 * 	The floor of the log (base 2) of x, or {@link Integer#MIN_VALUE}
	 * 	if x is zero or negative
	 */
	public static int log2(final double x) {
		return (x <= 0)
			? Integer.MIN_VALUE
			: (int)Math.floor((Math.log(x) / LN_2));
	}
}
