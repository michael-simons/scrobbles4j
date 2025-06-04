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
package scrobbles4j.client.sinks.dailyfratze;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
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
import scrobbles4j.model.PlayedTrack;
import scrobbles4j.model.Track;

/**
 * Basically recreates what I used to run as an Apple script.
 *
 * @author Michael J. Simons
 * @since 2021-10-06
 */
public final class DailyFratze extends AbstractSink {

	private static final Logger LOGGER = Logger.getLogger(DailyFratze.class.getName());

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

	/**
	 * Public constructor required by the Java Service API. Also used to initialize the
	 * user agent.
	 */
	public DailyFratze() {

		var descriptor = getClass().getModule().getDescriptor();
		this.ua = descriptor.name() + descriptor.version().map(v -> "/" + v).orElse("");
	}

	@Override
	public void init(Map<String, String> config) {

		doInitialize(() -> {
			this.bearerToken = Optional.ofNullable(config.get("bearerToken"))
				.map(String::trim)
				.filter(Predicate.not(String::isBlank))
				.orElseThrow(() -> new IllegalArgumentException("Bearer token is required."));

			this.endpoint = Optional.ofNullable(config.get("endpoint"))
				.map(String::trim)
				.filter(Predicate.not(String::isBlank))
				.map(URI::create)
				.orElseThrow(() -> new IllegalArgumentException("Endpoint is required."));

			this.encoder = switch (config.getOrDefault("encoder", "buggyEncoder")) {
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

		if (event.position() < track.duration() / 2.0) {
			return;
		}

		synchronized (this.latestTrackBySource) {
			var previousTrack = this.latestTrackBySource.get(event.source());
			if (previousTrack != null && previousTrack.equals(track)) {
				return;
			}

			try {
				var response = post(asProperties(event.track(), null, this.encoder));
				LOGGER.log(Level.INFO, "{0} ({1})", new Object[] { response.body(), response.statusCode() });
				this.latestTrackBySource.put(event.source(), track);
			}
			catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void consumeAll(Collection<PlayedTrack> playedTracks) {

		if (!isInitialized()) {
			throw new IllegalStateException("Sink is not initialized.");
		}

		for (var playedTrack : playedTracks) {
			try {
				var response = post(asProperties(playedTrack.track(), playedTrack.playedOn(), this.encoder));
				LOGGER.log(Level.INFO, "{0} ({1})", new Object[] { response.body(), response.statusCode() });
			}
			catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private HttpResponse<String> post(Map<String, String> properties) throws IOException, InterruptedException {
		var body = properties.entrySet()
			.stream()
			.map(entry -> String.format("%s=%s", entry.getKey(), this.encoder.apply(entry.getValue())))
			.collect(Collectors.joining("&"));

		var request = HttpRequest.newBuilder(this.endpoint)
			.header("User-Agent", this.ua)
			.header("Authorization", String.format("Bearer %s", this.bearerToken))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.build();

		return this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	/**
	 * {@return a buggy encoder, the same my old AppleScript scrobbler uses}
	 */
	static Function<String, String> buggyEncoder() {

		return s -> Optional.ofNullable(s)
			.map(value -> value.replaceAll("&", "%26").replaceAll(" ", "%20").replaceAll("\n", "%0A"))
			.orElse("");
	}

	/**
	 * {@return a sane URL encoder}
	 */
	static Function<String, String> newEncoder() {

		return s -> Optional.ofNullable(s).map(value -> URLEncoder.encode(s, StandardCharsets.UTF_8)).orElse("");
	}

	static Map<String, String> asProperties(Track track, Instant playedOn, Function<String, String> encoder) {

		// https://mizosoft.github.io/methanol/multipart_and_forms/
		// would be nicer, but as it turns out, the FormBodyPublisher does not allow for a
		// custom
		// encoder, which we need here to be able to recreate the old scrobblers behaviour

		var properties = new LinkedHashMap<String, String>();
		properties.put("artist[artist]", track.artist().name());
		properties.put("genre[genre]", track.genre().name());
		properties.put("track[name]", track.name());
		properties.put("track[played_count]", Integer.toString(track.playedCount() + ((playedOn != null) ? 0 : 1)));
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

		if (playedOn != null) {
			// This behaviour (system default zone) is actually the same as the previous
			// iTunes Script
			var playedOnInZone = playedOn.atZone(ZoneId.systemDefault());
			properties.put("date[y]", Integer.toString(playedOnInZone.getYear()));
			properties.put("date[M]", Integer.toString(playedOnInZone.getMonthValue()));
			properties.put("date[d]", Integer.toString(playedOnInZone.getDayOfMonth()));
			properties.put("date[h]", Integer.toString(playedOnInZone.getHour()));
			properties.put("date[m]", Integer.toString(playedOnInZone.getMinute()));
			properties.put("date[s]", Integer.toString(playedOnInZone.getSecond()));
		}

		return Collections.unmodifiableMap(properties);
	}

}
