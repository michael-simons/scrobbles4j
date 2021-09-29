package scrobbles4j.client.sources.api;

import scrobbles4j.model.Track;

import java.util.Optional;

/**
 * Provides access to the tracks of the implementing source.
 *
 * @author Michael J. Simons
 */
public interface Source {

	/**
	 * {@return the human readable name of this source}
	 */
	String getName();

	/**
	 * {@return the current state}
	 */
	default State getCurrentState() {
		return State.UNAVAILABLE;
	}

	/**
	 * {@return the latest track} If the latest track cannot be determined or hasn't changed since the last call
	 * the method should return an empty {@link Optional}.
	 */
	Optional<Track> getLatestTrack();
}
