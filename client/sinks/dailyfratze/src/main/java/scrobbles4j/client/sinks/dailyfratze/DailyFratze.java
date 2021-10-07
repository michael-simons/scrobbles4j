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
package scrobbles4j.client.sinks.dailyfratze;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import scrobbles4j.client.sinks.api.AbstractSink;
import scrobbles4j.client.sinks.api.PlayingTrackEvent;
import scrobbles4j.model.Track;

/**
 * @author Michael J. Simons
 * @since 2021-10-06
 */
public final class DailyFratze extends AbstractSink {

	private final Logger log = Logger.getLogger(DailyFratze.class.getName());

	/**
	 * Little surprising the client by which we access the endpoint.
	 */
	private final HttpClient httpClient = HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	/**
	 * Lookup of the latest track by source. Access must be synchronized.
	 */
	private final Map<String, Track> latestTrackBySource = new HashMap<>();

	/**
	 * The user agent.
	 */
	private final String ua;

	/**
	 * The actual endpoint that we post against.
	 */
	private URI endpoint;

	/**
	 * OAuth bearer token.
	 */
	private String bearerToken;

	/**
	 * How do we wanna encode things? Correct or broken as we used to.
	 */
	private Function<String, String> encoder;

	public DailyFratze() {

		var descriptor = getClass().getModule().getDescriptor();
		ua = descriptor.name() + descriptor.version().map(v -> "/" + v).orElse("");
	}

	@Override
	public void init(Map<String, String> config) {

		doInitialize(() -> {
			bearerToken = Optional.ofNullable(config.get("bearerToken"))
				.map(String::trim).filter(Predicate.not(String::isBlank)).orElseThrow(() -> new IllegalArgumentException("Bearer token is required."));

			endpoint = Optional.ofNullable(config.get("endpoint"))
				.map(String::trim)
				.filter(Predicate.not(String::isBlank))
				.map(URI::create)
				.orElseThrow(() -> new IllegalArgumentException("Endpoint is required."));

			encoder = switch (config.getOrDefault("encoder", "buggyEncoder")) {
				case "buggyEncoder" -> buggyEncoder();
				case "newEncoder" -> newEncoder();
				default -> throw new IllegalArgumentException("Unsupported encoder '" + config.get("encoder") + "'");
			};
		});
	}

	@Override
	public void onTrackPlaying(PlayingTrackEvent event) {

		if (!isInitialized()) {
			throw new IllegalStateException("Sink is not initialized.");
		}

		var track = event.track();

		if (event.position() < track.duration() / 2) {
			return;
		}

		synchronized (latestTrackBySource) {
			var previousTrack = latestTrackBySource.get(event.source());
			if (previousTrack != null && previousTrack.equals(track)) {
				return;
			}

			var body = asProperties(event, encoder).entrySet().stream()
				.map(entry -> String.format("%s=%s", entry.getKey(), encoder.apply(entry.getValue())))
				.collect(Collectors.joining("&"));

			var request = HttpRequest.newBuilder(endpoint)
				.header("User-Agent", ua)
				.header("Authorization", String.format("Bearer %s", bearerToken))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

			try {
				var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				log.log(Level.INFO, "{0} ({1})", new Object[] {response.body(), response.statusCode()});
				latestTrackBySource.put(event.source(), track);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@return a buggy encoder, the same my old AppleScript scrobbler uses}
	 */
	static Function<String, String> buggyEncoder() {

		return s -> Optional.ofNullable(s)
			.map(value -> value
				.replaceAll("&", "%26")
				.replaceAll(" ", "%20")
				.replaceAll("\n", "%0A"))
			.orElse("");
	}

	/**
	 * {@return a sane URL encoder}
	 */
	static Function<String, String> newEncoder() {

		return s -> Optional.ofNullable(s)
			.map(value -> URLEncoder.encode(s, StandardCharsets.UTF_8))
			.orElse("");
	}

	static Map<String, String> asProperties(PlayingTrackEvent event, Function<String, String> encoder) {

		var track = event.track();

		// https://mizosoft.github.io/methanol/multipart_and_forms/
		// would be nicer, but as it turns out, the FormBodyPublisher does not allow for a custom
		// encoder, which we need here to be able to recreate the old scrobblers behaviour

		var properties = new LinkedHashMap<String, String>();
		properties.put("artist[artist]", track.artist().name());
		properties.put("genre[genre]", track.genre().name());
		properties.put("track[name]", track.name());
		properties.put("track[played_count]", Integer.toString(event.currentPlayCount() + 1));
		if (track.rating() != null) {
			properties.put("track[rating]", Integer.toString(track.rating()));
		}
		if (track.duration() != null) {
			properties.put("track[duration]", Integer.toString(track.duration()));
		}
		properties.put("track[compilation]", track.compilation() ? "t" : "f");
		properties.put("track[album]", track.album());
		if (track.year() != null) {
			properties.put("track[year]", Integer.toString(track.year()));
		}
		if (track.discNumber() != null) {
			var discNumber = track.discNumber();
			if (discNumber.total() != null) {
				properties.put("track[disc_count]", Integer.toString(discNumber.total()));
			}
			properties.put("track[disc_number]", Integer.toString(discNumber.value()));
		}
		if (track.trackNumber() != null) {
			var trackNumber = track.trackNumber();
			if (trackNumber.total() != null) {
				properties.put("track[track_count]", Integer.toString(trackNumber.total()));
			}
			properties.put("track[track_number]", Integer.toString(trackNumber.value()));
		}
		properties.put("track[comment]", encoder.apply(track.comment()));

		return Collections.unmodifiableMap(properties);
	}
}
