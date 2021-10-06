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
package scrobbles4j.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 * @soundtrack Garbage - The World Is Not Enough
 * @since 2021-10-06
 */
class TrackTest {

	@Test
	void ofPropertiesShouldWork() {

		var properties = Map.<String, Object>ofEntries(
			Map.entry("artist", "Queen"),
			Map.entry("genre", "Rock"),
			Map.entry("name", "White Queen"),
			Map.entry("year", 1974),
			Map.entry("album", "Queen II"),
			Map.entry("trackNumber", 3),
			Map.entry("trackCount", 11),
			Map.entry("discNumber", 1),
			Map.entry("discCount", 2),
			Map.entry("duration", 123.0),
			Map.entry("rating", 5),
			Map.entry("comment", "Amazing")
		);

		var track = Track.of(properties);
		assertThat(track).extracting(Track::artist).extracting(Artist::name).isEqualTo("Queen");
		assertThat(track).extracting(Track::genre).extracting(Genre::name).isEqualTo("Rock");
		assertThat(track).extracting(Track::album).isEqualTo("Queen II");
		assertThat(track).extracting(Track::name).isEqualTo("White Queen");
		assertThat(track).extracting(Track::year).isEqualTo(1974);
		assertThat(track).extracting(Track::duration).isEqualTo(123);
		assertThat(track).extracting(Track::rating).isEqualTo(5);
		assertThat(track).extracting(Track::comment).isEqualTo("Amazing");
		assertThat(track).extracting(Track::trackNumber).isEqualTo(new TrackNumber(3, 11));
		assertThat(track).extracting(Track::discNumber).isEqualTo(new DiscNumber(1, 2));
		assertThat(track.compilation()).isFalse();
	}
}
