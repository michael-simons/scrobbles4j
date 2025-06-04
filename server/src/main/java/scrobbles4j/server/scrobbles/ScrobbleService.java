/*
 * Copyright 2021-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scrobbles4j.server.scrobbles;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Year;
import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import scrobbles4j.model.Artist;
import scrobbles4j.model.DiscNumber;
import scrobbles4j.model.Genre;
import scrobbles4j.model.PlayedTrack;
import scrobbles4j.model.Track;
import scrobbles4j.model.TrackNumber;

/**
 * Gets the latest scrobbles or creates new ones.
 *
 * @author Michael J. Simons
 */
@Singleton
final class ScrobbleService {

	private final Jdbi db;

	@Inject
	ScrobbleService(Jdbi db) {
		this.db = db;
	}

	/**
	 * {@return some statistics about the database}
	 */
	ScrobbleStats getScrobbleStats() {

		var statement = """
				SELECT min(played_on) AS first,
				       max(played_on) AS latest,
				       count(*) AS num_scrobbles
				FROM plays;
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement).map((rs, ctx) -> {
			var first = rs.getTimestamp("first");
			var latest = rs.getTimestamp("latest");
			return new ScrobbleStats((first != null) ? first.toInstant() : null,
					(latest != null) ? latest.toInstant() : null, rs.getInt("num_scrobbles"));
		}).one());
	}

	/**
	 * {@return years for which chart entries are available}
	 */
	Collection<Year> getAvailableYears() {

		return this.db.withHandle(handle -> handle.createQuery("SELECT distinct(year(played_on)) AS year FROM plays")
			.map((rs, ctx) -> Year.of(rs.getInt("year")))
			.stream()
			.sorted(Year::compareTo) // Don't want to bother the db
			.collect(Collectors.toList()));
	}

	/**
	 * The latest track before a given cut off date.
	 * @param maxResults the number of tracks to be received
	 * @param cutOffDate the date beyond which no tracks should be returned
	 * @return a collection of played tracks
	 */
	Collection<PlayedTrack> getLatest(int maxResults, Instant cutOffDate) {

		var statement = """
				SELECT a.artist, a.wikipedia_link, g.genre,
				       t.*,
				       p.played_on
				FROM plays p
				JOIN tracks t ON t.id = p.track_id
				JOIN artists a ON a.id = t.artist_id
				JOIN genres g ON g.id = t.genre_id
				WHERE p.played_on >= :cutOffDate
				ORDER BY p.played_on DESC
				LIMIT :maxResults
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("cutOffDate", Timestamp.from(cutOffDate))
			.bind("maxResults", maxResults)
			.map((rs, ctx) -> {

				var wikipediaLink = (rs.getString("wikipedia_link") != null)
						? URI.create(rs.getString("wikipedia_link")) : null;
				var artist = new Artist(rs.getString("artist"), wikipediaLink);
				var genre = new Genre(rs.getString("genre"));
				var trackNumber = new TrackNumber(rs.getInt("track_number"),
						rs.getObject("track_count", Integer.class));
				var discNumber = new DiscNumber(rs.getInt("disc_number"), rs.getObject("disc_count", Integer.class));

				var track = new Track(artist, genre, rs.getString("album"), rs.getString("name"),
						rs.getObject("year", Integer.class), rs.getObject("rating", Integer.class),
						rs.getObject("duration", Integer.class), rs.getObject("played_count", Integer.class),
						rs.getString("comment"), trackNumber, discNumber,
						"t".equalsIgnoreCase(rs.getString("compilation")));

				return new PlayedTrack(track, rs.getTimestamp("played_on").toInstant());
			})
			.collect(Collectors.toList()));
	}

}
