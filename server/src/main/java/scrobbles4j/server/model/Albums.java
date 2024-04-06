/*
 * Copyright 2021-2024 the original author or authors.
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
package scrobbles4j.server.model;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jdbi.v3.core.Jdbi;

import scrobbles4j.model.Artist;

/**
 * Repository for {@link Album albums}.
 *
 * @author Michael J. Simons
 */
@Singleton
public final class Albums {

	private final Jdbi db;

	@Inject
	Albums(Jdbi db) {
		this.db = db;
	}

	/**
	 * A collection of albums in which the given artist was involved. The list of artists will be empty if the album is
	 * defined only by the search artist
	 *
	 * @param artist The artist to look for
	 * @return Albums will be the key, and the value will be an optional list of artist that have published this album
	 */
	public Map<Album, Set<Artist>> findByArtist(Artist artist) {

		var statement = """
			SELECT DISTINCT t.album AS album_name, t.year AS album_year, a.artist AS artist_name
			FROM tracks t
			JOIN artists a ON a.id = t.artist_id
			WHERE (a.artist = :artist OR a.artist like '%& ' || :artist OR a.artist like :artist || ' &%')
			  AND t.year IS NOT NULL
			UNION ALL
			SELECT DISTINCT t.album AS album_name, t.year AS album_year, a.artist AS artist_name
			FROM tracks t
			JOIN artists a ON a.id = t.artist_id
			WHERE (lower(t.name) like lower('%[feat. ' || :artist || ']') OR lower(t.name) like lower('%[with ' || :artist || ']'))
			  AND t.year IS NOT NULL
			ORDER BY album_year, album_name
			""";

		return this.db.withHandle(handle -> handle
			.createQuery(statement)
			.bind("artist", artist.name())
			.reduceRows(new LinkedHashMap<>(), (m, rv) -> {
				var artists = m.computeIfAbsent(rv.getRow(Album.class), album -> new TreeSet<>(Comparator.comparing(Artist::name)));
				var albumArtist = rv.getRow(Artist.class);
				if (!albumArtist.name().equals(artist.name())) {
					artists.add(albumArtist);
				}
				return m;
			}));
	}
}
