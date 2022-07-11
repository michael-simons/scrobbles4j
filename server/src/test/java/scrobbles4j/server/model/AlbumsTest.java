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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import scrobbles4j.model.Artist;

@QuarkusTest
class AlbumsTest {

	@Inject
	Albums albums;

	@Test
	void findAlbumsByArtistsShouldWork() {

		var albumsByDangerDan = albums.findByArtist(new Artist("Danger Dan"));
		assertThat(albumsByDangerDan)
			.hasSize(2)
			.contains(
				Map.entry(new Album("Coming Out EP", 2008), Set.of()),
				Map.entry(new Album("Shibuya Crossing", 2018), Set.of(new Artist("Juse Ju")))
			);
	}
}
