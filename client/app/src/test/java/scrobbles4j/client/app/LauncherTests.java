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
package scrobbles4j.client.app;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import scrobbles4j.client.sinks.api.PlayingTrackEvent;
import scrobbles4j.client.sinks.api.Sink;
import scrobbles4j.client.sources.api.PlayingTrack;
import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michael J. Simons
 */
class LauncherTests {

	@Test
	void includedSourcesShouldWork() {

		var aSource = new ASource();
		assertThat(Launcher.includeSourcePredicate(Set.of("A Source")).test(aSource)).isTrue();
		assertThat(Launcher.includeSourcePredicate(Set.of("ASource")).test(aSource)).isTrue();
		assertThat(Launcher.includeSourcePredicate(Set.of("asource")).test(aSource)).isTrue();
		assertThat(Launcher.includeSourcePredicate(Set.of("asource ")).test(aSource)).isTrue();

		assertThat(Launcher.includeSourcePredicate(Set.of("B Source")).test(aSource)).isFalse();
		assertThat(Launcher.includeSourcePredicate(Set.of("bSource")).test(aSource)).isFalse();
		assertThat(Launcher.includeSourcePredicate(Set.of(" bsource")).test(aSource)).isFalse();
	}

	@Test
	void extractingPropertiesShouldWork() {

		var config = Map.of("asink.a", "a", "aSink.b", "b", "nothing", "x", "unrelated.y", "y", "ASink.c", "c");
		var finalConfig = Launcher.extractConfigFor(new ASink(), config);
		assertThat(finalConfig).containsAllEntriesOf(Map.of("a", "a", "b", "b", "c", "c"));
	}

	static class ASource implements Source {

		@Override
		public Optional<PlayingTrack> getCurrentTrack() {
			return Optional.empty();
		}

		@Override
		public State getCurrentState() {
			return State.PLAYING;
		}

	}

	static class ASink implements Sink {

		@Override
		public void onTrackPlaying(PlayingTrackEvent event) {
		}

	}

}
