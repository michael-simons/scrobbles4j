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
package scrobbles4j.client.sources.apple.itunes;

import scrobbles4j.client.sources.api.PlayingTrack;
import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;

import java.util.Optional;

import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.macos.itunes.Application;
import scrobbles4j.model.Track;

/**
 * A source that watches Apple iTunes.
 *
 * @author Michael J. Simons
 */
public final class AppleiTunes implements Source {

	private final Application application = Application.getInstance();

	@Override
	public String getDisplayName() {
		return "Apple iTunes™";
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
}
