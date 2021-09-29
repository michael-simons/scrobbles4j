package scrobbles4j.client.app;

import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;

import java.util.ServiceLoader;

public final class Launcher {

	public static void main(String... args) {

		var services = ServiceLoader.load(Source.class);
		services.iterator().forEachRemaining(s -> {
			var state = s.getCurrentState();
			if (state == State.UNAVAILABLE) {
				System.out.println(s.getName() + " is not available and will not be watched.");
				return;
			}
			System.out.println(s.getCurrentState());
		});
	}
}
