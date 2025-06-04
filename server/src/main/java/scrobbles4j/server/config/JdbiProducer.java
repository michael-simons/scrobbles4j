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
package scrobbles4j.server.config;

import java.net.URI;

import javax.sql.DataSource;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import scrobbles4j.model.Artist;
import scrobbles4j.server.model.Album;

/**
 * Creates a global instance of {@link Jdbi} based on the one {@link DataSource}.
 *
 * @author Michael J. Simons
 */
public final class JdbiProducer {

	/**
	 * Creates a new JDBI datasource.
	 * @param dataSource the datasource for which Jdbi should be produced
	 * @return a Jdbi instance
	 * @throws Exception for everything that can happen while determining model
	 * constructors
	 */
	@Produces
	@Singleton
	public Jdbi jdbi(DataSource dataSource) throws Exception {

		return Jdbi.create(dataSource).registerColumnMapper(URI.class, (rs, col, ctx) -> {
			var value = rs.getString(col);
			if (value == null || value.isBlank()) {
				return null;
			}
			return URI.create(value);
		})
			.registerRowMapper(Album.class, ConstructorMapper.of(Album.class, "album"))
			.registerRowMapper(Artist.class, ConstructorMapper.of(Artist.class.getConstructor(String.class), "artist"));
	}

}
