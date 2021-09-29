package scrobbles4j.model;

/**
 * Representation of a disc number (usually a pair of the actual disc number and the total number of discs in a set.
 *
 * @author Michael J. Simons
 */
record DiscNumber(int value, Integer total) {

	DiscNumber(int value) {
		this(value, null);
	}
}