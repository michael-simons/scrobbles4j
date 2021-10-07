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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jdbi.v3.core.Jdbi;

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
			.createQuery("SELECT a.artist FROM artists a WHERE lower(a.artist) = lower(:artist)")
			.bind("artist", name)
			.map((rs, ctx) -> new Artist(rs.getString(1)))
			.findOne()
		);
	}
}
