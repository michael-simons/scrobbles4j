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
package scrobbles4j.server.charts;

import scrobbles4j.model.Artist;

import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jdbi.v3.core.Jdbi;

/**
 * This service is responsible for retrieving chart data via SQL from a datasource.
 *
 * @author Michael J. Simons
 */
@Singleton
final class ChartService {

	private final Jdbi db;

	@Inject
	ChartService(Jdbi db) {
		this.db = db;
	}

	Map<Integer, List<EntryArtist>> getFavoriteArtistsByYears(int maxRank, int numYears) {

		var statement = """
			WITH rank_per_year AS (
			  SELECT year(p.played_on) as year,
			         a.artist,
			         dense_rank() OVER (partition by year(played_on) ORDER BY count(*) DESC) AS rank
			  FROM plays p
			  JOIN tracks t ON t.id = p.track_id
			  JOIN artists a ON a.id = t.artist_id
			  WHERE year(p.played_on) between :minYear AND :maxYear
			    AND t.compilation = 'f'
			  GROUP BY year(p.played_on), a.artist
			)
			SELECT year, artist, rank,
			       ifnull(
			         lag(rank) OVER (partition by artist ORDER by year ASC) - rank,
			         'new'
				     )  as `change`
			FROM rank_per_year
			WHERE rank <= :maxRank
			ORDER BY year DESC, rank ASC
			""";

		var currentYear = ZonedDateTime.now().getYear();
		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.bind("minYear", currentYear - numYears)
			.bind("maxYear", currentYear)
			.scanResultSet((resultSetSupplier, ctx) -> {
				var result = new LinkedHashMap<Integer, List<EntryArtist>>();
				try (ctx; var rs = resultSetSupplier.get()) {
					while (rs.next()) {
						var year = result.computeIfAbsent(rs.getInt("year"), k -> new ArrayList<>());
						year.add(new EntryArtist(rs.getInt("rank"), rs.getString("artist"), rs.getString("change")));
					}
				}
				return result;
			}));
	}

	/**
	 * The top n tracks without restrictions of a year.
	 * @param maxRank The maximum rank to be included 
	 * @return A list of chart entries for tracks
	 * @see  #getTopNTracks(int, Optional, Optional)
	 */
	Collection<EntryTrack> getTopNTracks(int maxRank) {

		return getTopNTracks(maxRank, Optional.empty(), Optional.empty());
	}

	/**
	 * @param maxRank The maximum rank to be included
	 * @param year    An optional year in which the entries had been play
	 * @return A list of chart entries for tracks
	 */
	Collection<EntryTrack> getTopNTracks(int maxRank, Optional<Integer> year, Optional<Artist> artist) {

		var statement = """
			SELECT * FROM (
			  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
			         count(*) as cnt,
			         a.artist,
			         t.name
			  FROM plays p
			  JOIN tracks t ON t.id = p.track_id
			  JOIN artists a ON a.id = t.artist_id
			  WHERE (:year IS NULL OR year(p.played_on) = :year)
			    AND (:artist IS NULL OR lower(a.artist) = lower(:artist))
			  GROUP BY a.artist, t.name
			) src
			WHERE rank <= :maxRank
			ORDER BY rank ASC
			""";

		return this.db.withHandle(handle -> {
			var query = handle.createQuery(statement);
			year.ifPresentOrElse(value -> query.bind("year", value), () -> query.bindNull("year", Types.INTEGER));
			artist.map(Artist::name).ifPresentOrElse(value -> query.bind("artist", value), () -> query.bindNull("artist", Types.CHAR));
			return query
				.bind("maxRank", maxRank)
				.map((rs, ctx) -> new EntryTrack(rs.getInt("rank"), rs.getInt("cnt"), rs.getString("artist"),
					rs.getString("name")))
				.collect(Collectors.toList());
		});
	}

	/**
	 * @param maxRank The maximum rank to be included
	 * @param year    The year in which the entries had been play
	 * @return A list of chart entries for albums
	 */
	Collection<EntryAlbum> getTopNAlbums(int maxRank, int year) {

		var statement = """
			SELECT * FROM (
			  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
			         a.artist,
			         t.album
			  FROM plays p
			  JOIN tracks t ON t.id = p.track_id
			  JOIN artists a ON a.id = t.artist_id
			  WHERE year(p.played_on) = :year
			    AND t.compilation = 'f'
			  GROUP BY a.artist, t.album
			) src
			WHERE rank <= :maxRank
			ORDER BY rank ASC;
			""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.bind("year", year)
			.map((rs, ctx) -> new EntryAlbum(rs.getInt("rank"), rs.getString("artist"), rs.getString("album")))
			.collect(Collectors.toList()));
	}

	/**
	 * @param maxRank The maximum rank to be included
	 * @param year    The year in which the entries had been play
	 * @return A list of chart entries for albums
	 */
	Collection<EntryArtist> getTopNArtists(int maxRank, int year) {

		var statement = """
			SELECT * FROM (
			  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
			         a.artist
			  FROM plays p
			  JOIN tracks t ON t.id = p.track_id
			  JOIN artists a ON a.id = t.artist_id
			  WHERE year(p.played_on) = :year
			    AND t.compilation = 'f'
			  GROUP BY a.artist
			) src
			WHERE rank <= :maxRank
			ORDER BY rank ASC;
			""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.bind("year", year)
			.map((rs, ctx) -> new EntryArtist(rs.getInt("rank"), rs.getString("artist")))
			.collect(Collectors.toList()));
	}
}
