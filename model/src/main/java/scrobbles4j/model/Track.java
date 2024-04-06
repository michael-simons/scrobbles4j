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
package scrobbles4j.model;

import java.util.Map;

/**
 * A track.
 *
 * @author Michael J. Simons
 * @param artist      Artist of this track
 * @param genre       Optional genre
 * @param album       Album this track appeared on
 * @param name        Name of this track
 * @param year        Optional year the track was published
 * @param duration    Optional duration of this track
 * @param rating      Optional rating
 * @param playedCount The number of times this track was played
 * @param comment     Optional comment
 * @param trackNumber Track number
 * @param discNumber  Disc number
 * @param compilation Flag if the album of this track is a compilation
 */
public record Track(
	Artist artist,
	Genre genre,
	String album,
	String name,
	Integer year,
	Integer duration,
	Integer rating,
	Integer playedCount,
	String comment,
	TrackNumber trackNumber,
	DiscNumber discNumber,
	boolean compilation
) {

	/**
	 * Create a new track from a set of properties
	 * @param properties The properties (modelled after Apples properties)
	 * @return A new track
	 */
	public static Track of(Map<String, Object> properties) {

		var trackNumber = properties.containsKey("trackNumber") ?
			new TrackNumber((int) properties.get("trackNumber"), (Integer) properties.get("trackCount")) : null;

		var discNumber = properties.containsKey("discNumber") ?
			new DiscNumber((int) properties.get("discNumber"), (Integer) properties.get("discCount")) : null;

		return new Track(
			new Artist((String) properties.get("artist")),
			new Genre((String) properties.get("genre")),
			(String) properties.get("album"),
			(String) properties.get("name"),
			properties.containsKey("year") ? (Integer) properties.get("year") : null,
			properties.containsKey("duration") ? Math.toIntExact(Math.round((Double) properties.get("duration"))) : null,
			properties.containsKey("rating") ? (Integer) properties.get("rating") : null,
			properties.containsKey("playedCount") ? (Integer) properties.get("playedCount") : null,
			(String) properties.get("comment"),
			trackNumber,
			discNumber,
			(boolean) properties.getOrDefault("compilation", false)
		);
	}
}
