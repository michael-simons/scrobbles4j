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
package scrobbles4j.server.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import scrobbles4j.model.Artist;

/**
 * Repository for artists.
 *
 * @author Michael J. Simons
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
	 * Finds a single artist by name.
	 * @param name the name of the artist
	 * @return an optional, normalized artist
	 */
	public Optional<Artist> findByName(String name) {

		return this.db.withHandle(handle -> handle.registerRowMapper(this.canonicalArtistMapper)
			.createQuery(
					"SELECT a.artist AS name, a.wikipedia_link FROM artists a WHERE lower(a.artist) = lower(:artist)")
			.bind("artist", name)
			.mapTo(Artist.class)
			.findOne());
	}

	/**
	 * Retrieves the summary from the Artists main wikipedia page if available.
	 * @param artist the artist to retrieve the summary for
	 * @return an optional summary
	 */
	@CacheResult(cacheName = "artist-summary")
	public Optional<String> getSummary(Artist artist) {

		var wikipediaLink = artist.wikipediaLink();
		if (wikipediaLink == null) {
			return Optional.empty();
		}
		var langAndTitle = extractLanguageAndTitle(wikipediaLink);
		var api = URI.create("https://%s.wikipedia.org/api/rest_v1/page/summary/%s".formatted(langAndTitle.lang(),
				langAndTitle.title()));
		try {
			var response = this.httpClient.send(HttpRequest.newBuilder().uri(api).build(),
					HttpResponse.BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				return Optional.empty();
			}
			var summary = this.objectMapper.readTree(response.body());
			return Optional.of(summary.get("extract").textValue());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}

	}

	/**
	 * Retrieves a Wikimedia image for a given artist.
	 * @param artist the artist for which the lead image should be returned
	 * @return an optional wikimedia image
	 */
	@CacheResult(cacheName = "artist-image")
	public Optional<WikimediaImage> getImage(Artist artist) {

		var wikipediaLink = artist.wikipediaLink();
		if (wikipediaLink == null) {
			return Optional.empty();
		}
		var langAndTitle = extractLanguageAndTitle(wikipediaLink);
		var api = URI.create("https://%s.wikipedia.org/api/rest_v1/page/media-list/%s".formatted(langAndTitle.lang(),
				langAndTitle.title()));
		try {
			var response = this.httpClient.send(HttpRequest.newBuilder().uri(api).build(),
					HttpResponse.BodyHandlers.ofInputStream());

			if (response.statusCode() != 200) {
				return Optional.empty();
			}
			var mediaList = this.objectMapper.readTree(response.body());
			JsonNode image = null;
			for (var item : mediaList.get("items")) {
				if (!"image".equals(item.get("type").asText())) {
					continue;
				}
				if (image == null) {
					image = item;
				}
				if (item.get("leadImage").asBoolean()) {
					image = item;
					break;
				}
			}

			return Optional.ofNullable(image)
				.map(v -> new WikimediaImage(wikipediaLink,
						URI.create("https:%s".formatted(v.get("srcset").get(0).get("src").asText()))));
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

	private static LanguageAndTitle extractLanguageAndTitle(URI wikipediaLink) {
		var lang = wikipediaLink.getHost().substring(0, wikipediaLink.getHost().indexOf("."));
		var title = wikipediaLink.getPath().substring(wikipediaLink.getPath().lastIndexOf("/") + 1);
		return new LanguageAndTitle(lang, title);
	}

	/**
	 * Finds related artists.
	 * @param artist the artist to which related artists shall be found
	 * @return a list of related artists
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

		return this.db.withHandle(
				handle -> handle.createQuery(statement).bind("artist", artist.name()).mapTo(Artist.class).list());
	}

	private record LanguageAndTitle(String lang, String title) {
	}

}
