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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import scrobbles4j.client.sinks.api.Sink;
import scrobbles4j.client.sinks.api.SinkComparator;
import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;

/**
 * The clients launcher. Will orchestrate sinks and sources.
 *
 * @author Michael J. Simons
 */
@Command(name = "scrobbles4j", mixinStandardHelpOptions = true)
public final class Launcher implements Runnable {

	private final Logger log = Logger.getLogger(Launcher.class.getName());

	@Option(names = "--source", description = """
			The sources to use, valid values are `Apple Music` and `Apple iTunes`. \
			If no sources are given, all available sources will be activated. \
			The values are case insensitive and white spaces will be ignored. \
			Specify multiple times for multiple sources.
			""")
	Set<String> includedSources = Set.of();

	@Option(names = "--sink", description = """
			The sinks to use. If no sinks are given, all default sinks will be used. \
			Specify multiple times for multiple sinks.
			""")
	Set<String> includedSinks = Set.of();

	@Option(names = { "-c", "--config" }, description = """
			Set of config options. Prefix with the name of the source or sink.
			""")
	Map<String, String> config = Map.of();

	/**
	 * Standard entry point.
	 * @param args all args which are passed to PicoCLI.
	 */
	public static void main(String... args) {

		new CommandLine(new Launcher()).execute(args);
	}

	/**
	 * Subcommand that publishes only the selected tracks (in iTunes or Music).
	 */
	@Command(name = "publish-selected-tracks", description = "Use this option to publish selected tracks only.")
	public void publishSelectedTracks() {

		var sources = getSources();
		var sinks = getSinks();

		sinks.forEach(sink -> sink.init(extractConfigFor(sink, this.config)));

		if (sources.isEmpty()) {
			this.log.warning("No sources");
		}
		else if (sinks.isEmpty()) {
			this.log.warning("No sinks");
		}
		else {
			var allTracks = sources.stream().flatMap(source -> source.getSelectedTracks().stream()).toList();
			sinks.forEach(sink -> sink.consumeAll(allTracks));
		}
	}

	@Override
	public void run() {

		var sources = getSources();
		var sinks = getSinks();

		sinks.forEach(sink -> sink.init(extractConfigFor(sink, this.config)));

		if (this.log.isLoggable(Level.INFO) && !(sources.isEmpty() || sinks.isEmpty())) {
			var joiner = Collectors.joining(", ", "'", "'");
			var sourceNames = sources.stream().map(Source::getDisplayName).collect(joiner);
			var sinkNames = sinks.stream().map(s -> s.getClass().getSimpleName()).collect(joiner);
			this.log.log(Level.INFO, "Connecting the following sources {0} with these sinks {1}.",
					new Object[] { sourceNames, sinkNames });
		}

		var scheduler = Executors.newScheduledThreadPool(sources.size());

		sources.stream()
			.map(source -> new Pipe(source, sinks))
			.forEach(pipe -> scheduler.scheduleWithFixedDelay(pipe::trigger, 0, 5, TimeUnit.SECONDS));

		Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
	}

	private List<Source> getSources() {

		return ServiceLoader.load(Source.class)
			.stream()
			.map(ServiceLoader.Provider::get)
			.filter(includeSourcePredicate(this.includedSources))
			.toList();
	}

	private List<Sink> getSinks() {

		return ServiceLoader.load(Sink.class)
			.stream()
			.map(ServiceLoader.Provider::get)
			.filter(includeSinkPredicate(this.includedSinks))
			.sorted(SinkComparator.INSTANCE)
			.toList();
	}

	static Predicate<Source> includeSourcePredicate(Set<String> includedSources) {
		var normalizedSources = includedSources.stream()
			.map(s -> s.toLowerCase(Locale.ENGLISH).trim().replaceAll("\\s", ""))
			.collect(Collectors.toSet());

		Predicate<Source> includeSource = source -> normalizedSources.isEmpty()
				|| normalizedSources.contains(source.getClass().getSimpleName().toLowerCase(Locale.ENGLISH));
		return includeSource.and(source -> source.getCurrentState() != State.UNAVAILABLE);
	}

	static Predicate<Sink> includeSinkPredicate(Set<String> includedSinks) {
		var normalizedSinks = includedSinks.stream()
			.map(s -> s.toLowerCase(Locale.ENGLISH).trim().replaceAll("\\s", ""))
			.collect(Collectors.toSet());

		return sink -> (includedSinks.isEmpty() && sink.isActiveByDefault())
				|| normalizedSinks.contains(sink.getClass().getSimpleName().toLowerCase(Locale.ENGLISH));
	}

	static Map<String, String> extractConfigFor(Sink sink, Map<String, String> allConfig) {

		var prefix = Pattern.compile(Pattern.quote(sink.getClass().getSimpleName()) + "\\.", Pattern.CASE_INSENSITIVE);
		var config = new HashMap<String, String>();
		allConfig.forEach((k, v) -> {
			var matcher = prefix.matcher(k);
			if (matcher.find()) {
				config.put(matcher.replaceAll(""), v);
			}
		});
		return Map.copyOf(config);
	}

}
