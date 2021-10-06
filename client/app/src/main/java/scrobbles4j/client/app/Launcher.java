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
package scrobbles4j.client.app;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import scrobbles4j.client.sinks.api.Sink;
import scrobbles4j.client.sinks.api.SinkComparator;
import scrobbles4j.client.sources.api.Source;
import scrobbles4j.client.sources.api.State;

import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The clients launcher. Will orchestrate sinks and sources.
 *
 * @author Michael J. Simons
 */
@Command(name = "scrobbles4j", mixinStandardHelpOptions = true)
public final class Launcher implements Runnable {

	@Option(names = "--sources", defaultValue = "", description = """
		The sources to use, valid values are `Apple Music` and `Apple iTunes`. \
		If no sources are given, all available sources will be activated. \
		The values are case insensitive and white spaces will be ignored.
		""")
	Set<String> includedSources;

	public static void main(String... args) {

		new CommandLine(new Launcher()).execute(args);
	}

	@Override
	public void run() {

		var sources = ServiceLoader.load(Source.class).stream()
			.map(ServiceLoader.Provider::get)
			.filter(includeSourcePredicate(includedSources))
			.collect(Collectors.toList());

		var sinks = ServiceLoader.load(Sink.class).stream()
			.map(ServiceLoader.Provider::get)
			.sorted(SinkComparator.INSTANCE)
			.collect(Collectors.toList());

		var scheduler = Executors.newScheduledThreadPool(sources.size());

		sources.stream()
			.map(source -> new Pipe(source, sinks))
			.forEach(pipe -> scheduler.scheduleWithFixedDelay(pipe::trigger, 0, 5, TimeUnit.SECONDS));

		Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
	}

	static Predicate<Source> includeSourcePredicate(Set<String> includedSources) {
		var normalizedSources = includedSources.stream()
			.map(s -> s.toLowerCase(Locale.ENGLISH).trim().replaceAll("\\s", ""))
			.collect(Collectors.toSet());

		Predicate<Source> includeSource = source -> normalizedSources.isEmpty() || normalizedSources.contains(source.getClass().getSimpleName().toLowerCase(Locale.ENGLISH));
		return includeSource.and(source -> source.getCurrentState() != State.UNAVAILABLE);
	}
}
