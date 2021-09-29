package scrobbles4j.client.sources.api;

/**
 * Represents the state of a {@link Source}.
 *
 * @author Michael J. Simons
 */
public enum State {

	/**
	 * Source is in an unknown state.
	 */
	UNKNOWN,
	/**
	 * Source is stopped. A track maybe available.
	 */
	STOPPED,
	/**
	 * Source is currently playing. A track should be available if more than halve of it has been played.
	 */
	PLAYING,
	/**
	 * Source is unavailable
	 */
	UNAVAILABLE
}
