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

import scrobbles4j.model.PlayedTrack;

import java.util.Collection;
import java.util.Map;

/**
 * A sink consuming playing tracks.
 *
 * @author Michael J. Simons
 */
public interface Sink {

	/**
	 * {@return the order in which this sink should be called} A higher value means higher priority.
	 */
	default int getOrder() {
		return 0;
	}

	/**
	 * {@return true if this sink is active by default}
	 */
	default boolean isActiveByDefault() {
		return false;
	}

	/**
	 * Configures this sink. Only called once.
	 *
	 * @param config The config map for this sink. Prefixes will be removed.
	 */
	default void init(Map<String, String> config) {
	}

	/**
	 * This method is called by a source when a track is playing.
	 *
	 * @param event An event containing the playing track
	 */
	void onTrackPlaying(PlayingTrackEvent event);

	/**
	 * Consumes a list of played tracks in bulk.
	 *
	 * @param playedTracks A collection of played tracks
	 */
	default void consumeAll(Collection<PlayedTrack> playedTracks) {
	}
}
