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
/**
 * This module combines all known sources and sinks into a standalone app, that can listen to either Apple iTunes, Music or both and log the tracks received.
 * @author Michael J. Simons
 */
module scrobbles4j.client.app {
	requires info.picocli;
	requires java.logging;
	requires scrobbles4j.client.sources.api;
	requires scrobbles4j.client.sinks.api;

	uses scrobbles4j.client.sources.api.Source;
	uses scrobbles4j.client.sinks.api.Sink;

	opens scrobbles4j.client.app to info.picocli;
}
