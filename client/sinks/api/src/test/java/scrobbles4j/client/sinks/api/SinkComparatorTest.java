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
package scrobbles4j.client.sinks.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 */
class SinkComparatorTest {

	@Test
	void higherValuesShouldHavePrecedenceBecauseIHateTheWaySpringHasIt() {

		var s1 = new Sink() {
			@Override
			public void onTrackPlaying(PlayingTrackEvent event) {
			}

			@Override public int getOrder() {
				return 1;
			}
		};

		var s2 = new Sink() {
			@Override
			public void onTrackPlaying(PlayingTrackEvent event) {
			}

			@Override public int getOrder() {
				return 42;
			}
		};

		assertThat(List.of(s1, s2).stream().sorted(SinkComparator.INSTANCE))
			.containsExactly(s2, s1);
	}
}