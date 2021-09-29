package scrobbles4j.client.sources.apple.music;

import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;
import scrobbles4j.model.Track;

import java.util.Optional;

import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.macos.music.Application;

public final class SourceImpl implements Source {

	private final Application application = Application.getInstance();

	@Override
	public String getName() {
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
			return State.UNAVAILABLE;
		}
	}

	@Override
	public Optional<Track> getLatestTrack() {
		return Optional.empty();
	}
}
