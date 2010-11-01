package org.bodytrack.client;

/**
 * A class that describes a tile, whether arrived or in transit.
 *
 * Encapsulates a (level, offset) pair.  Note that objects of this
 * class are immutable, at least in Java.  In JavaScript, it is
 * certainly possible for an attacker to modify objects of this
 * class, if that attacker can introduce cross-site scripting.
 */
public class TileDescription {
	private int level;
	private int offset;

	/**
	 * Creates a new TileDescription.
	 *
	 * @param level
	 * 		the level for the tile this TileDescription describes
	 * @param offset
	 * 		the offset for the tile this TileDescription describes
	 */
	public TileDescription(int level, int offset) {
		this.level = level;
		this.offset = offset;
	}

	/**
	 * Returns the level of the tile this TileDescription describes.
	 *
	 * @return
	 * 		the level of the tile this TileDescription describes
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Returns the offset of the tile this TileDescription describes.
	 *
	 * @return
	 * 		the offset of the tile this TileDescription describes
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Returns the width of the tile this describes, as a double.
	 *
	 * @return
	 * 		the width, in seconds, of the tile with the level and
	 * 		offset specified in this object
	 */
	public double getTileWidth() {
		return Math.pow(2, level) * GrapherTile.TILE_WIDTH;
	}

	/**
	 * Returns the minimum time possible for a data point inside the
	 * tile this TileDescription describes.
	 *
	 * @return
	 * 		the minimum possible time which could be associated with
	 * 		a data point in a tile described by this TileDescription
	 */
	public double getMinTime() {
		return getOffset() * getTileWidth();
	}

	/**
	 * Returns the maximum time possible for a data point inside the
	 * tile this TileDescription describes.
	 *
	 * @return
	 * 		the maximum possible time which could be associated with
	 * 		a data point in a tile described by this TileDescription
	 */
	public double getMaxTime() {
		return getMinTime() + getTileWidth();
	}

	/**
	 * Returns a hash code for this object.
	 *
	 * This method of computing hash codes works best when level and
	 * offset both fit in 16 bits (they are both between -32768 and
	 * 32767, inclusive).  This should be the case for most instances
	 * of this class.
	 *
	 * @return
	 * 		an integer that will be the same for objects that are equal,
	 * 		and should be different for unequal objects, at least most
	 * 		of the time
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (level << 16) + offset;
	}

	/**
	 * Indicates whether this object is equal to o.
	 *
	 * If o is not a TileDescription, or if o is a TileDescription
	 * with different level or different offset, returns
	 * <tt>false</tt>.  Otherwise, returns <tt>true</tt>.
	 *
	 * @return
	 * 		<tt>true</tt> if and only if o is a non-<tt>null</tt>
	 * 		TileDescription with the same level and offset as
	 * 		this
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
}
