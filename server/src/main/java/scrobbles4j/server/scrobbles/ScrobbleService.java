/*
 * Copyright 2021 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scrobbles4j.server.scrobbles;

import scrobbles4j.model.Artist;
import scrobbles4j.model.DiscNumber;
import scrobbles4j.model.Genre;
import scrobbles4j.model.Track;
import scrobbles4j.model.TrackNumber;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jdbi.v3.core.Jdbi;

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

		return this.db.withHandle(handle -> handle.createQuery(statement).map((rs, ctx) -> new ScrobbleStats(
			rs.getTimestamp("first").toInstant(),
			rs.getTimestamp("latest").toInstant(),
			rs.getInt("num_scrobbles")
		)).one());
	}

	/**
	 * The latest track before a given cut off date
	 * @param maxResults The number of tracks to be received
	 * @param cutOffDate The date beyond which no tracks should be returned
	 * @return A collection of played tracks
	 */
	Collection<PlayedTrack> getLatest(int maxResults, Instant cutOffDate) {

		var statement = """
			SELECT a.artist, g.genre,
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

				var artist = new Artist(rs.getString("artist"));
				var genre = new Genre(rs.getString("genre"));
				var trackNumber = new TrackNumber(rs.getInt("track_number"),
					rs.getObject("track_count", Integer.class));
				var discNumber = new DiscNumber(rs.getInt("disc_number"), rs.getObject("disc_count", Integer.class));

				var track = new Track(
					artist, genre,
					rs.getString("album"), rs.getString("name"),
					rs.getObject("year", Integer.class),
					rs.getObject("rating", Integer.class),
					rs.getObject("duration", Integer.class),
					rs.getString("comment"),
					trackNumber, discNumber,
					"t".equalsIgnoreCase(rs.getString("compilation"))
				);

				return new PlayedTrack(track, rs.getTimestamp("played_on").toInstant());
			})
			.collect(Collectors.toList()));
	}
}
