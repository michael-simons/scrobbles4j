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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import scrobbles4j.model.Artist;

@QuarkusTest
class ArtistsTest {

	@Inject
	Artists artists;

	@Test
	void findByNameShouldWork() {

		var artist = artists.findByName("Queen");
		assertThat(artist).hasValue(new Artist("Queen"));
	}

	@Test
	void getAlbumsByArtistsShouldWork() {

		var relatedToQueen = artists.findRelated(new Artist("Queen"));
		assertThat(relatedToQueen)
			.hasSize(2)
			.extracting(Artist::name)
			.containsExactly("Freddie Mercury", "Roger Taylor");
	}
}
