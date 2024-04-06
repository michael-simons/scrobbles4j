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
package scrobbles4j.client.sources.api;

import scrobbles4j.model.PlayedTrack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Provides access to the tracks of the implementing source.
 *
 * @author Michael J. Simons
 */
public interface Source {

	/**
	 * {@return the human readable name of this source}
	 */
	default String getDisplayName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * {@return the current state}
	 */
	default State getCurrentState() {
		return State.UNAVAILABLE;
	}

	/**
	 * {@return the current playing track} If the latest track cannot be determined the method should return an empty {@link Optional}.
	 */
	Optional<PlayingTrack> getCurrentTrack();

	/**
	 * {@return the current selection of played tracks}
	 */
	default Collection<PlayedTrack> getSelectedTracks() {
		return List.of();
	}
}
