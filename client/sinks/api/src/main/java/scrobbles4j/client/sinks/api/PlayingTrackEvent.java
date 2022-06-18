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
package scrobbles4j.client.sinks.api;

import scrobbles4j.model.Track;

/**
 * Event when a track being played is published.
 *
 * @author Michael J. Simons
 * @param track            The track that is actual playing
 * @param position         The current position
 * @param seenBefore       True if this the track has been seen before
 * @param source           Name of the source that published the track
 */
public record PlayingTrackEvent(Track track, double position, boolean seenBefore, String source) {
}
