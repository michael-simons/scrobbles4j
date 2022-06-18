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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import scrobbles4j.client.sinks.api.PlayingTrackEvent;
import scrobbles4j.model.Artist;
import scrobbles4j.model.DiscNumber;
import scrobbles4j.model.Genre;
import scrobbles4j.model.Track;
import scrobbles4j.model.TrackNumber;

/**
 * @author Michael J. Simons
 * @since 2021-10-06
 */
@WireMockTest
class DailyFratzeTest {

	@Test
	void shouldRecreateOldFormat(WireMockRuntimeInfo wmRuntimeInfo) {

		var sink = new DailyFratze();
		sink.init(Map.of("bearerToken", "Braunbaer", "endpoint", wmRuntimeInfo.getHttpBaseUrl() + "/api/scrobbles/new"));

		var track = new Track(
			new Artist("Swiss + Die Andern"),
			new Genre("Punk"),
			"Orphan",
			"Punkrock Karate [mit Mortis & Mal Élevé]",
			2021, 291, 40, 1, "ripped & encoded by MJS", new TrackNumber(9, 11), new DiscNumber(1, 2), false);

		stubFor(post(urlPathEqualTo("/api/scrobbles/new")).willReturn(ok("Scrobbled '" + track.name() + "'")));

		sink.onTrackPlaying(new PlayingTrackEvent(track, track.duration() / 2.0 + 1, false, "foo"));
		sink.onTrackPlaying(new PlayingTrackEvent(track, track.duration() / 2.0 + 10, false, "foo"));

		verify(1, postRequestedFor(urlPathEqualTo("/api/scrobbles/new"))
			.withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
			.withHeader("Authorization", equalTo("Bearer Braunbaer"))
			.withRequestBody(
				containing("""
					artist[artist]=Swiss%20+%20Die%20Andern&\
					genre[genre]=Punk&\
					track[name]=Punkrock%20Karate%20[mit%20Mortis%20%26%20Mal%20Élevé]&\
					track[played_count]=2&\
					track[rating]=40&\
					track[duration]=291&\
					track[compilation]=f&\
					track[album]=Orphan&\
					track[year]=2021&\
					track[disc_count]=2&\
					track[disc_number]=1&\
					track[track_count]=11&\
					track[track_number]=9&\
					track[comment]=ripped%20%26%20encoded%20by%20MJS\
					"""
				)
			));
	}
}
