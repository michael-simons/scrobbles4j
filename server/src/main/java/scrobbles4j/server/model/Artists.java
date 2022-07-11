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
package scrobbles4j.server.model;

import scrobbles4j.model.Artist;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/**
 * Repository for artists.
 *
 * @author Michael J. Simons
 * @soundtrack Eric Fish - The Weeping Song
 * @since 2021-10-07
 */
@Singleton
public final class Artists {

	private final Jdbi db;

	@Inject
	Artists(Jdbi db) {
		this.db = db;
	}

	/**
	 * @param name The name of the artist
	 * @return an optional, normalized artist
	 */
	public Optional<Artist> findByName(String name) {

		return this.db.withHandle(handle -> handle
			.registerRowMapper(ConstructorMapper.factory(Artist.class))
			.createQuery("SELECT a.artist AS name FROM artists a WHERE lower(a.artist) = lower(:artist)")
			.bind("artist", name)
			.mapTo(Artist.class)
			.findOne()
		);
	}

	/**
	 * Finds related artist
	 *
	 * @param artist The artist to which related artists shall be found
	 * @return A list of related artists
	 */
	public List<Artist> findRelated(Artist artist) {

		var statement = """
			SELECT tgt.artist AS name
			FROM artists a
			JOIN related_artists r ON r.source_id = a.id
			JOIN artists tgt ON tgt.id = r.target_id
			WHERE a.artist = :artist
			UNION
			SELECT tgt.artist AS name
			FROM artists a
			JOIN related_artists r ON r.target_id = a.id
			JOIN artists tgt ON tgt.id = r.source_id
			WHERE a.artist = :artist
			UNION
			SELECT a.artist AS name
			FROM tracks t
			JOIN artists a ON a.id = t.artist_id
			WHERE (lower(t.name) like lower('%[feat. ' || :artist || ']') OR lower(t.name) like lower('%[with ' || :artist || ']'))
			  AND t.year IS NOT NULL
			ORDER BY name
			""";

		return this.db.withHandle(handle -> handle
			.registerRowMapper(ConstructorMapper.factory(Artist.class))
			.createQuery(statement)
			.bind("artist", artist.name())
			.mapTo(Artist.class)
			.list()
		);
	}
}
