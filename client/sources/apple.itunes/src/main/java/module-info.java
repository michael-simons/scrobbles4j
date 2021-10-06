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
import scrobbles4j.client.sources.apple.itunes.AppleiTunes;

/**
 * Implementation of the sources API via Apple Music and <a href="https://github.com/hendriks73/obstunes">Obstunes</a>.
 *
 * @author Michael J. Simons
 */
module scrobbles4j.client.sources.apple.itunes {

	provides scrobbles4j.client.sources.api.Source with AppleiTunes;

	requires scrobbles4j.client.sources.api;
	requires tagtraum.obstunes;
}
