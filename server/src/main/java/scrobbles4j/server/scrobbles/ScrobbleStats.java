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
package scrobbles4j.server.scrobbles;

import java.time.Instant;

/**
 * @author Michael J. Simons
 * @param first        moment in time of the first scrobble
 * @param latest       moment in time of the latest scrobble
 * @param numScrobbles how many tracks have been scrobbled
 */
public record ScrobbleStats(Instant first, Instant latest, int numScrobbles) {

	/**
	 * {@return true if there are no scrobbles}
	 */
	public boolean isEmpty() {
		return numScrobbles == 0;
	}
}
