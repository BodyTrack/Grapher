package org.bodytrack.client;

/**
 * A class that describes a tile, whether arrived or in transit.
 *
 * <p>
 * Encapsulates a (level, offset) pair.  Note that objects of this
 * class are immutable, at least in Java.  In JavaScript, it is
 * certainly possible for an attacker to modify objects of this
 * class, if that attacker can execute code via cross-site scripting.
 * </p>
 */
public final class TileDescription {
	private int level;
	private long offset;

	/**
	 * Creates a new TileDescription.
	 *
	 * @param level
	 * 	The level for the tile this TileDescription describes
	 * @param offset
	 * 	The offset for the tile this TileDescription describes
	 */
	public TileDescription(int level, long offset) {
		this.level = level;
		this.offset = offset;
	}

	public int getLevel() {
		return level;
	}

	public long getOffset() {
		return offset;
	}

	/**
	 * Returns the width of the tile this describes, as a double.
	 */
	public double getTileWidth() {
		return Math.pow(2, level) * GrapherTile.TILE_WIDTH;
	}

	/**
	 * Returns the minimum time possible for a data point inside the
	 * tile this TileDescription describes.
	 */
	public double getMinTime() {
		return getOffset() * getTileWidth();
	}

	/**
	 * Returns the maximum time possible for a data point inside the
	 * tile this TileDescription describes.
	 */
	public double getMaxTime() {
		return getMinTime() + getTileWidth();
	}

	/**
	 * Returns a hash code for this object.
	 *
	 * @return
	 * 	An integer that will be the same for objects that are equal,
	 * 	and should usually be different for unequal objects
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (level * 31) + (int)(offset >> 32) + (int)offset;
	}

	/**
	 * Indicates whether this object is equal to o.
	 *
	 * <p>
	 * If o is not a {@link TileDescription}, or if o is a
	 * {@link TileDescription} with different level or different offset,
	 * returns <tt>false</tt>.  Otherwise, returns <tt>true</tt>.
	 * </p>
	 *
	 * @return
	 * 	<tt>true</tt> if and only if o is a non-<tt>null</tt>
	 * 	TileDescription with the same level and offset as this
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (! (o instanceof TileDescription))
			return false;

		TileDescription other = (TileDescription) o;

		return (level == other.level) && (offset == other.offset);
	}

    /**
     * Returns a short string uniquely representing this {@link TileDescription}.
     *
     * @return
     * 	The returned string is suitable to use as a key in a string-keyed
     * 	hash table
     */
	public String getTileKey() {
		return level + "." + offset;
	}

	/**
	 * Computes the tile level corresponding to a length of time.
	 * Returns a double;  consider using Math.{floor,round,ceil} to convert to integer.
	 *
	 * @param timeLength
	 * 	Length of time, in seconds
	 */
	public static double computeLevel(double timeLength) {
		return Math.log(timeLength/GrapherTile.TILE_WIDTH)/Math.log(2);
	}

	/**
	 * Compute TileDescription for tile at a given level containing a given time.
	 *
	 * @param level
	 * 	The level for the TileDescription to be returned
	 * @param time
	 * 	Timestamp contained in tile (seconds since the epoch)
	 */
	public static TileDescription tileAt(int level, double time) {
		return new TileDescription(level,
			(int)Math.floor(time / (Math.pow(2, level) * GrapherTile.TILE_WIDTH)));
	}
}
