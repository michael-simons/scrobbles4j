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
package scrobbles4j.server.charts;

import java.net.URI;
import java.sql.Types;
import java.time.Duration;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import scrobbles4j.model.Artist;
import scrobbles4j.server.model.Album;

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

	/**
	 * Return stats for the given year.
	 * @param year the year in question
	 * @return stats for the year
	 */
	YearStats getStats(Year year) {

		var statement = """
				WITH prev AS (SELECT count(*) cnt, :year - 1 v FROM plays WHERE year(played_on) = :year - 1),
				     next AS (SELECT count(*) cnt, :year + 1 v FROM plays WHERE year(played_on) = :year + 1)
				SELECT prev.v AS previous_year, next.v AS next_year, count(*) AS num_scrobbles, sum(duration) AS total_duration
				FROM tracks t JOIN plays p ON t.id = p.track_id
				LEFT OUTER JOIN prev ON prev.cnt <> 0
				LEFT OUTER JOIN next ON next.cnt <> 0
				WHERE year(p.played_on) = :year
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("year", year.getValue())
			.map((rs, ctx) -> new YearStats(year,
					Optional.ofNullable(rs.getObject("previous_year", Integer.class)).map(Year::of).orElse(null),
					Optional.ofNullable(rs.getObject("next_year", Integer.class)).map(Year::of).orElse(null),
					rs.getInt("num_scrobbles"), Duration.ofSeconds(rs.getLong("total_duration"))))
			.one());
	}

	/**
	 * Gets the topn n artists by year.
	 * @param maxRank the maximum entries
	 * @param numYears the maximum number of years
	 * @return the top n artists by year
	 */
	Map<Integer, List<EntryArtist>> getTopNArtistsByYear(int maxRank, int numYears) {

		var statement = """
				WITH rank_per_year AS (
				   SELECT year(p.played_on) as year,
				         a.artist,
				         dense_rank() OVER (partition by year(played_on) ORDER BY count(*) DESC) AS rank
				  FROM plays p
				  JOIN tracks t ON t.id = p.track_id
				  JOIN artists a ON a.id = t.artist_id
				  WHERE year(p.played_on) BETWEEN :minYear - 1 AND :maxYear
				    AND t.compilation = 'f'
				  GROUP BY year(p.played_on), a.artist
				), unfiltered AS (
				  SELECT year, artist, rank,
				         CASE
				           WHEN lag(year) OVER (partition by artist ORDER by year ASC) != year -1 THEN 'new'
				           ELSE ifnull(lag(rank) OVER (partition by artist ORDER by year ASC) - rank, 'new')
				         END as `change`
				  FROM rank_per_year
				  WHERE rank <= :maxRank
				)
				SELECT * FROM unfiltered
				WHERE year >= :minYear
				ORDER BY year DESC, rank ASC, artist ASC
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
	 * Gets the top N tracks.
	 * @param maxRank the maximum rank to be included
	 * @param year an optional year in which the entries had been play
	 * @param artist an optional artist to query for
	 * @param includeCompilations a flag to include tracks from compilations or not
	 * @return a list of chart entries for tracks
	 */
	Collection<EntryTrack> getTopNTracks(int maxRank, Optional<Year> year, Optional<Artist> artist,
			boolean includeCompilations) {

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
				    AND nvl(:compilation, t.compilation) = 'f'
				  GROUP BY a.artist, t.name
				  HAVING count(*) >= 2
				) src
				WHERE rank <= :maxRank
				ORDER BY rank ASC
				""";

		return this.db.withHandle(handle -> {
			var query = handle.createQuery(statement);
			year.map(Year::getValue)
				.ifPresentOrElse(value -> query.bind("year", value), () -> query.bindNull("year", Types.INTEGER));
			artist.map(Artist::name)
				.ifPresentOrElse(value -> query.bind("artist", value), () -> query.bindNull("artist", Types.CHAR));
			return query.bind("maxRank", maxRank)
				.bind("compilation", includeCompilations ? "f" : null)
				.map((rs, ctx) -> new EntryTrack(rs.getInt("rank"), rs.getInt("cnt"), rs.getString("artist"),
						rs.getString("name")))
				.collectIntoList();
		});
	}

	/**
	 * Gets the top N new tracks in a year.
	 * @param maxRank the maximum rank to be included
	 * @param year the year in which the tracks have been released and played the first
	 * time
	 * @return a list of chart entries for tracks
	 */
	Collection<EntryTrack> getTopNNewTracksInYear(int maxRank, Year year) {

		var statement = """
				WITH new_tracks AS (
				  SELECT id, year FROM tracks t
				  WHERE t.year = :year AND EXISTS( SELECT id FROM plays p WHERE p.track_id = t.id AND year(p.played_on) = t.year)
				)
				SELECT * FROM (
				  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
				         count(*) as cnt,
				         a.artist,
				         t.name
				  FROM new_tracks
				  JOIN plays p ON p.track_id = new_tracks.id
				  JOIN tracks t ON t.id = p.track_id
				  JOIN artists a ON a.id = t.artist_id
				  WHERE year(p.played_on) = new_tracks.year
				    AND t.compilation = 'f'
				  GROUP BY a.artist, t.name
				  HAVING count(*) >= 2
				) src
				WHERE rank <= :maxRank
				ORDER BY rank ASC
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("year", year.getValue())
			.bind("maxRank", maxRank)
			.map((rs, ctx) -> new EntryTrack(rs.getInt("rank"), rs.getInt("cnt"), rs.getString("artist"),
					rs.getString("name")))
			.collectIntoList());
	}

	/**
	 * Gets the top N albums.
	 * @param maxRank the maximum rank to be included
	 * @param year the year in which the entries had been play
	 * @return a list of chart entries for albums
	 */
	Collection<EntryAlbum> getTopNAlbums(int maxRank, Optional<Year> year) {

		var statement = """
				SELECT * FROM (
				  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
				         a.artist,
				         t.album
				  FROM plays p
				  JOIN tracks t ON t.id = p.track_id
				  JOIN artists a ON a.id = t.artist_id
				  WHERE (:year IS NULL OR year(p.played_on) = :year)
				    AND t.compilation = 'f'
				  GROUP BY a.artist, t.album
				) src
				WHERE rank <= :maxRank
				ORDER BY rank ASC
				""";

		return this.db.withHandle(handle -> {
			var query = handle.createQuery(statement);
			year.map(Year::getValue)
				.ifPresentOrElse(value -> query.bind("year", value), () -> query.bindNull("year", Types.INTEGER));
			return query.bind("maxRank", maxRank)
				.map((rs, ctx) -> new EntryAlbum(rs.getInt("rank"), rs.getString("artist"), rs.getString("album")))
				.collectIntoList();
		});
	}

	/**
	 * Gets the top N albums in a month.
	 * @param maxRank the maximum rank to be included
	 * @param month the month in which the entries had been played
	 * @return a list of chart entries for albums
	 */
	Collection<RankedEntry<Album>> getTopNAlbums(int maxRank, YearMonth month) {

		var statement = """
				SELECT * FROM (
				  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
				         count(*) AS cnt,
				         t.album,
				         t.year
				  FROM plays p
				  JOIN tracks t ON t.id = p.track_id
				  JOIN artists a ON a.id = t.artist_id
				  WHERE year(p.played_on) = :year and month(p.played_on) = :month
				  GROUP BY a.artist, t.album
				) src
				WHERE rank <= :maxRank
				ORDER BY rank ASC
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.bind("year", month.getYear())
			.bind("month", month.getMonthValue())
			.map((rs, ctx) -> new RankedEntry<>(rs.getInt("rank"), rs.getInt("cnt"),
					new Album(rs.getString("album"), rs.getInt("year"))))
			.collectIntoList());
	}

	/**
	 * Gets the top N artists in a year.
	 * @param maxRank the maximum rank to be included
	 * @param year the year in which the entries had been play
	 * @return a list of chart entries for artists
	 */
	Collection<EntryArtist> getTopNArtists(int maxRank, Year year) {

		var statement = """
				WITH
				  rank_per_year AS (
				    SELECT year(p.played_on) as year,
				           a.artist,
				           dense_rank() OVER (partition by year(played_on) ORDER BY count(*) DESC) AS rank
				    FROM plays p
				    JOIN tracks t ON t.id = p.track_id
				    JOIN artists a ON a.id = t.artist_id
				    WHERE year(p.played_on) BETWEEN :year - 1 AND :year
				      AND t.compilation = 'f'
				    GROUP BY year(p.played_on), a.artist
				  ),
				  unfiltered AS (
				    SELECT year, artist, rank,
				           CASE
				             WHEN lag(year) OVER (partition by artist ORDER by year ASC) != year -1 THEN 'new'
				             ELSE ifnull(lag(rank) OVER (partition by artist ORDER by year ASC) - rank, 'new')
				           END as `change`
				    FROM rank_per_year
				    WHERE rank <= :maxRank
				  )
				SELECT *
				FROM unfiltered
				WHERE year = :year
				ORDER BY rank ASC, artist ASC
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.bind("year", year.getValue())
			.map((rs, ctx) -> new EntryArtist(rs.getInt("rank"), rs.getString("artist"), rs.getString("change")))
			.collectIntoList());
	}

	/**
	 * Top artists by month (compilations included).
	 * @param maxRank the maximum rank to be included
	 * @param month the month in which the entries had been play
	 * @return a list of chart entries for artists
	 */
	Collection<RankedEntry<Artist>> getTopNArtists(int maxRank, YearMonth month) {

		var statement = """
				SELECT * FROM (
				     SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
				            count(*) as cnt,
				            a.artist,
				            a.wikipedia_link
				    FROM plays p
				    JOIN tracks t ON t.id = p.track_id
				    JOIN artists a ON a.id = t.artist_id
				    WHERE year(p.played_on) = :year and month(p.played_on) = :month
				    GROUP BY a.artist, a.wikipedia_link
				) src
				WHERE rank <= :maxRank
				ORDER BY rank, artist
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.bind("year", month.getYear())
			.bind("month", month.getMonthValue())
			.map((rs, ctx) -> {
				var wikipediaLink = (rs.getString("wikipedia_link") != null)
						? URI.create(rs.getString("wikipedia_link")) : null;
				return new RankedEntry<>(rs.getInt("rank"), rs.getInt("cnt"),
						new Artist(rs.getString("artist"), wikipediaLink));
			})
			.collectIntoList());
	}

	/**
	 * Gets the top N artists overall.
	 * @param maxRank the maximum rank to be included
	 * @return a list of chart entries for artists
	 */
	Collection<EntryArtist> getTopNArtists(int maxRank) {

		var statement = """
				SELECT * FROM (
				  SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
				         a.artist
				  FROM plays p
				  JOIN tracks t ON t.id = p.track_id
				  JOIN artists a ON a.id = t.artist_id
				  WHERE t.compilation = 'f'
				  GROUP BY a.artist
				) src
				WHERE rank <= :maxRank
				ORDER BY rank ASC
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("maxRank", maxRank)
			.map((rs, ctx) -> new EntryArtist(rs.getInt("rank"), rs.getString("artist"), null))
			.collectIntoList());
	}

	Collection<FavoriteTrack> getTop1Tracks(YearMonth month) {
		var statement = """
				SELECT * FROM (
				     SELECT dense_rank() OVER (ORDER BY count(*) DESC) AS rank,
				            count(*) as cnt,
				            a.artist,
				            t.name
				    FROM plays p
				    JOIN tracks t ON t.id = p.track_id
				    JOIN artists a ON a.id = t.artist_id
				    WHERE year(p.played_on) = :year and month(p.played_on) = :month
				    GROUP BY a.artist, t.name
				) src
				WHERE rank = 1
				ORDER BY artist
				""";

		return this.db.withHandle(handle -> handle.createQuery(statement)
			.bind("year", month.getYear())
			.bind("month", month.getMonthValue())
			.map((rs, ctx) -> new FavoriteTrack(rs.getString("artist"), rs.getString("name"), rs.getInt("cnt")))
			.collectIntoList());
	}

}
