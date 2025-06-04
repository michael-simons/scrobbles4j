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

import java.net.URI;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import scrobbles4j.model.Artist;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class AlbumsTests {

	@Inject
	Albums albums;

	@Test
	void findAlbumsByArtistsShouldWork() {

		var albumsByDangerDan = this.albums
			.findByArtist(new Artist("Danger Dan", URI.create("https://de.wikipedia.org/wiki/Danger_Dan")));
		assertThat(albumsByDangerDan).hasSize(4)
			.contains(Map.entry(new Album("Coming Out EP", 2008), Set.of()),
					Map.entry(new Album("Shibuya Crossing", 2018), Set.of(new Artist("Juse Ju"))),
					Map.entry(new Album("Traurige Clowns", 2010), Set.of(new Artist("Koljah & Danger Dan"))),
					Map.entry(new Album("Aschenbecher", 2012), Set.of(new Artist("Danger Dan & NMZS"))));
	}

}
