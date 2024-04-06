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
package scrobbles4j.client.sinks.logger;

import scrobbles4j.client.sinks.api.PlayingTrackEvent;
import scrobbles4j.client.sinks.api.Sink;
import scrobbles4j.model.PlayedTrack;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author Michael J. Simons
 */
public final class LoggingSink implements Sink {

	private final Logger targetLog = Logger.getLogger(LoggingSink.class.getName());

	@Override
	public boolean isActiveByDefault() {
		return true;
	}

	@Override
	public void onTrackPlaying(PlayingTrackEvent event) {

		targetLog.info("Playing track " + event.track() + " at position " + event.position() + (event.seenBefore() ?
			"" :
			" (changed since last event)"));
	}

	@Override
	public void consumeAll(Collection<PlayedTrack> playedTracks) {

		targetLog.info("Received the following tracks");
		playedTracks.forEach(playedTrack -> targetLog.info(() -> String.format("%s played on %s", playedTrack.track(), playedTrack.playedOn())));
	}
}
