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
package scrobbles4j.server.charts;

import java.time.Duration;
import java.time.Year;

/**
 * @author Michael J. Simons
 */
public record YearStats(
	/**
	 * The year in question.
	 */
	Year year,
	/**
	 * Number of tracks played in the given year.
	 */
	int numScrobbles,
	/**
	 * Total time
	 */
	Duration totalDuration
) {

	public String formattedTotalDuration() {
		if (totalDuration.isZero()) {
			return "";
		}
		return String.format("%d days, %d hours and %d minutes", totalDuration.toDaysPart(),
			totalDuration.toHoursPart(),
			totalDuration.toMinutesPart());
	}

	public boolean empty() {
		return totalDuration.isZero() || numScrobbles == 0;
	}
}
