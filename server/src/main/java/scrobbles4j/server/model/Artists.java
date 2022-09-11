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

import com.fasterxml.jackson.databind.ObjectMapper;
import scrobbles4j.model.Artist;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapperFactory;
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

	private final ObjectMapper objectMapper;

	private final RowMapperFactory canonicalArtistMapper;

	private final HttpClient httpClient;

	@Inject
	Artists(Jdbi db, HttpClient httpClient, ObjectMapper objectMapper) throws NoSuchMethodException {
		this.db = db;
		this.canonicalArtistMapper = ConstructorMapper.factory(Artist.class.getConstructor(String.class, URI.class));
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * @param name The name of the artist
	 * @return an optional, normalized artist
	 */
	public Optional<Artist> findByName(String name) {

		return this.db.withHandle(handle -> handle
			.registerRowMapper(canonicalArtistMapper)
			.createQuery("SELECT a.artist AS name, a.wikipedia_link FROM artists a WHERE lower(a.artist) = lower(:artist)")
			.bind("artist", name)
			.mapTo(Artist.class)
			.findOne()
		);
	}

	/**
	 * Retrieves the summary from the Artists main wikipedia page if available.
	 *
	 * @param artist The artist to retrieve the summary for
	 * @return An optional future summary
	 */
	public CompletableFuture<Optional<String>> getSummary(Artist artist) {

		var wikipediaLink = artist.wikipediaLink();
		if (wikipediaLink == null) {
			return CompletableFuture.completedFuture(Optional.empty());
		}
		var lang = wikipediaLink.getHost().substring(0, wikipediaLink.getHost().indexOf("."));
		var title = wikipediaLink.getPath().substring(wikipediaLink.getPath().lastIndexOf("/") + 1);
		var api = URI.create("https://%s.wikipedia.org/api/rest_v1/page/summary/%s".formatted(lang, title));
		return httpClient
			.sendAsync(HttpRequest.newBuilder().uri(api).build(), HttpResponse.BodyHandlers.ofInputStream())
			.thenApply(response -> {
				if (response.statusCode() != 200) {
					return Optional.empty();
				}
				try {
					var summary = objectMapper.readTree(response.body());
					return Optional.of(summary.get("extract").textValue());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
	}

	/**
	 * Finds related artist
	 *
	 * @param artist The artist to which related artists shall be found
	 * @return A list of related artists
	 */
	public List<Artist> findRelated(Artist artist) {

		var statement = """
			SELECT tgt.artist AS artist_name
			FROM artists a
			JOIN related_artists r ON r.source_id = a.id
			JOIN artists tgt ON tgt.id = r.target_id
			WHERE a.artist = :artist
			UNION
			SELECT tgt.artist AS artist_name
			FROM artists a
			JOIN related_artists r ON r.target_id = a.id
			JOIN artists tgt ON tgt.id = r.source_id
			WHERE a.artist = :artist
			UNION
			SELECT a.artist AS artist_name
			FROM tracks t
			JOIN artists a ON a.id = t.artist_id
			WHERE (lower(t.name) like lower('%[feat. ' || :artist || ']') OR lower(t.name) like lower('%[with ' || :artist || ']'))
			  AND t.year IS NOT NULL
			ORDER BY artist_name
			""";

		return this.db.withHandle(handle -> handle
			.createQuery(statement)
			.bind("artist", artist.name())
			.mapTo(Artist.class)
			.list()
		);
	}
}
