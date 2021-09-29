package scrobbles4j.model;

/**
 * Representation of a track number (usually a pair of the actual track number and the total number of tracks on an album.
 *
 * @author Michael J. Simons
 */
record TrackNumber(int value, Integer total) {

	TrackNumber(int value) {
		this(value, null);
	}
}