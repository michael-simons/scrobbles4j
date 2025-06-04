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
import scrobbles4j.client.sources.apple.music.AppleMusic;

/**
 * Implementation of the sources API via Apple Music and <a href="https://github.com/hendriks73/obstmusic">Obstmusic</a>.
 *
 * @author Michael J. Simons
 */
module scrobbles4j.client.sources.apple.music {

	provides scrobbles4j.client.sources.api.Source with AppleMusic;

	requires java.logging;
	requires scrobbles4j.client.sources.api;
	requires tagtraum.obstmusic;
}
