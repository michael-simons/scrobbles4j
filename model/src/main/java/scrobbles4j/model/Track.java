package scrobbles4j.model;

/**
 * A track.
 *
 * @author Michael J. Simons
 */
public record Track(Artist artist, Genre genre, String name, String album, Integer year, TrackNumber tracknumber,
					DiscNumber discNumber, Integer rating, String comment, boolean compilation, Double duration) {
}
