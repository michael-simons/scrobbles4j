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
package scrobbles4j.model;

/**
 * Representation of a track number (usually a pair of the actual track number and the
 * total number of tracks on an album.
 *
 * @author Michael J. Simons
 * @param value the actual track number
 * @param total optional total number of tracks on the album the track belongs to
 */
public record TrackNumber(int value, Integer total) {

	TrackNumber(int value) {
		this(value, null);
	}
}
