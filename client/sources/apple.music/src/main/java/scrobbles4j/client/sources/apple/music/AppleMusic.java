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
package scrobbles4j.client.sources.apple.music;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.macos.music.Application;
import scrobbles4j.client.sources.api.PlayingTrack;
import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;
import scrobbles4j.model.PlayedTrack;
import scrobbles4j.model.Track;

/**
 * @author Michael J. Simons
 */
public final class AppleMusic implements Source {

	private final Logger log = Logger.getLogger(AppleMusic.class.getName());

	private final Application application = Application.getInstance();

	@Override
	public String getDisplayName() {
		return "Apple Music™";
	}

	@Override
	public State getCurrentState() {

		try {
			return switch (application.getPlayerState()) {
				case STOPPED, PAUSED -> State.STOPPED;
				case PLAYING -> State.PLAYING;
				default -> State.UNKNOWN;
			};
		} catch (JaplScriptException e) {
			log.log(Level.WARNING, this.getDisplayName() + " is unavailable.", e);
			return State.UNAVAILABLE;
		}
	}

	@Override
	public Optional<PlayingTrack> getCurrentTrack() {

		var trackHandle = application.getCurrentTrack();
		if (trackHandle == null) {
			return Optional.empty();
		}

		var track = Track.of(trackHandle.getProperties());
		return Optional.of(new PlayingTrack(track, application.getPlayerPosition()));
	}

	@Override
	public Collection<PlayedTrack> getSelectedTracks() {

		return Arrays.stream(application.getSelection().cast(com.tagtraum.macos.music.Track[].class))
			.filter(trackHandle -> trackHandle.getProperties().containsKey("playedDate"))
			.map(trackHandle -> {
				var track = Track.of(trackHandle.getProperties());
				return new PlayedTrack(track, trackHandle.getPlayedDate().toInstant());
			})
			.toList();
	}
}
