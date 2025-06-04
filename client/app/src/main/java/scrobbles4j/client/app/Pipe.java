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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import scrobbles4j.client.sinks.api.PlayingTrackEvent;
import scrobbles4j.client.sinks.api.Sink;
import scrobbles4j.client.sources.api.PlayingTrack;
import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;
import scrobbles4j.model.Track;

/**
 * Connects a source and a sink.
 *
 * @author Michael J. Simons
 */
final class Pipe {

	private static final Logger LOGGER = Logger.getLogger(Pipe.class.getName());

	private final Source source;

	private final List<Sink> sinks;

	Track lastTrack;

	Pipe(Source source, List<Sink> sinks) {
		this.source = source;
		this.sinks = sinks;
	}

	void trigger() {

		var currentTrack = Optional.<PlayingTrack>empty();
		try {
			if (this.source.getCurrentState() == State.PLAYING) {
				currentTrack = this.source.getCurrentTrack();
			}
		}
		catch (Exception ex) {
			LOGGER.log(Level.WARNING, ex, () -> "Could not retrieve current track from source '%s' in state %s"
				.formatted(this.source.getDisplayName(), State.PLAYING));
		}

		currentTrack.map(this::createEvent)
			.stream()
			.flatMap(event -> this.sinks.stream().map(sink -> (Runnable) () -> sink.onTrackPlaying(event)))
			.sequential()
			.forEach(CompletableFuture::runAsync);
	}

	PlayingTrackEvent createEvent(PlayingTrack playingTrack) {
		boolean seenBefore = playingTrack.track().equals(this.lastTrack);
		this.lastTrack = playingTrack.track();
		return new PlayingTrackEvent(playingTrack.track(), playingTrack.position(), seenBefore,
				this.source.getDisplayName());
	}

}
