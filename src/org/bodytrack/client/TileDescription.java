package org.bodytrack.client;

/**
 * A class that describes a tile, whether arrived or in transit.
 *
 * <p>Encapsulates a (level, offset) pair.  Note that objects of this
 * class are immutable, at least in Java.  In JavaScript, it is
 * certainly possible for an attacker to modify objects of this
 * class, if that attacker can introduce cross-site scripting.</p>
 */
public final class TileDescription {
	private int level;
	private long offset;

	/**
	 * Creates a new TileDescription
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

	/**
	 * Returns the level of the tile this TileDescription describes
	 *
	 * @return
	 * 	The level of the tile this TileDescription describes
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Returns the offset of the tile this TileDescription describes
	 *
	 * @return
	 * 	The offset of the tile this TileDescription describes
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Returns the width of the tile this describes, as a double
	 *
	 * @return
	 * 	The width, in seconds, of the tile with the level returned
	 * 	by {@link #getLevel()}
	 */
	public double getTileWidth() {
		return Math.pow(2, level) * GrapherTile.TILE_WIDTH;
	}

	/**
	 * Returns the minimum time possible for a data point inside the
	 * tile this {@link TileDescription} describe.
	 *
	 * @return
	 * 	The minimum possible time which could be associated with
	 * 	a data point in a tile described by this {@link TileDescription}
	 */
	public double getMinTime() {
		return getOffset() * getTileWidth();
	}

	/**
	 * Returns the maximum time possible for a data point inside the
	 * tile this {@link TileDescription} describes
	 *
	 * @return
	 * 	The maximum possible time which could be associated with
	 * 	a data point in a tile described by this {@link TileDescription}
	 */
	public double getMaxTime() {
		return getMinTime() + getTileWidth();
	}

	/**
	 * Returns a hash code for this object
	 *
	 * <p>This method of computing hash codes works best when level and
	 * offset both fit in 16 bits (they are both between -32768 and
	 * 32767, inclusive).  This should be the case for most instances
	 * of this class.  Note that this method still works even when
	 * level and offset do not both fit in 16 bits, though.</p>
	 *
	 * @return
	 * 	An integer that will be the same for objects that are equal,
	 * 	and should usually be different for unequal objects
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (level << 16) + (int)offset;
	}

	/**
	 * Indicates whether this object is equal to o
	 *
	 * <p>If o is not a {@link TileDescription}, or if o is a {@link TileDescription}
	 * with different level or different offset, returns <code>false</code>.
	 * Otherwise, returns <tt>true</tt>.</p>
	 *
	 * @param o
	 * 	The object to check for equality with
	 * @return
	 * 	<code>true</code> if and only if o is a non-<code>null</code>
	 * 	{@link TileDescription} with the same level and offset as this
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

		if (level != other.level)
			return false;
		if (offset != other.offset)
			return false;

		return true;
	}

	/**
	 * Returns a short string uniquely representing this {@link TileDescription}
	 *
	 * @return
	 * 	The returned string is suitable to use as a key in a string-keyed
	 * 	hash table
	 */
	public String getTileKey() {
		return level + "." + offset;
	}
	
	/**
	 * Computes the tile level corresponding to a length of time
	 *
	 * <p>Returns a double; consider using {@link Math#floor(double) Math.floor},
	 * {@link Math#round(double) Math.round}, or {@link Math#ceil(double) Math.ceil}
	 * to convert to an integer.</p>
	 *
	 * @param timeLength
	 * 	Length of time, in seconds
	 */
	public static double computeLevel(double timeLength) {
		return Math.log(timeLength/GrapherTile.TILE_WIDTH)/Math.log(2);
	}

	/**
	 * Builds a {@link TileDescription} for tile at a given level containing a
	 * given time
	 *
	 * @param level
	 * 	The level for the {@link TileDescription} to be returned
	 * @param time
	 * 	Timestamp contained in tile (standard Unix timestamp as used by BodyTrack:
	 * 	seconds since 1/1/1970)
	 */
	public static TileDescription tileAt(int level, double time) {
		return new TileDescription(level,
			(long) Math.floor(time / (Math.pow(2, level) * GrapherTile.TILE_WIDTH)));
	}
}
