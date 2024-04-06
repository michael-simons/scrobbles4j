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
import scrobbles4j.client.sinks.logger.LoggingSink;

/**
 * @author Michael J. Simons
 */
module scrobbles4j.client.sinks.logger {

	provides scrobbles4j.client.sinks.api.Sink with LoggingSink;

	requires scrobbles4j.client.sinks.api;
	requires java.logging;
}
