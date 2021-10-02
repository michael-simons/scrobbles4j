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
package scrobbles4j.model;

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
	String comment,
	TrackNumber trackNumber,
	DiscNumber discNumber,
	boolean compilation
) {
}
