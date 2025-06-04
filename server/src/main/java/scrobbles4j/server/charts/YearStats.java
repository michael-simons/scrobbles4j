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
package scrobbles4j.server.charts;

import java.time.Duration;
import java.time.Year;

/**
 * Stats per year.
 *
 * @author Michael J. Simons
 * @param year the year in question
 * @param previousYear the previous year
 * @param nextYear the next year
 * @param numScrobbles number of tracks played in the given year
 * @param totalDuration total time
 */
public record YearStats(Year year, Year previousYear, Year nextYear, int numScrobbles, Duration totalDuration) {

	/**
	 * {@return a formatted, total duration (in days, hours and minutes) of playtime}
	 */
	public String formattedTotalDuration() {
		if (this.totalDuration.isZero()) {
			return "";
		}
		return String.format("%d days, %d hours and %d minutes", this.totalDuration.toDaysPart(),
				this.totalDuration.toHoursPart(), this.totalDuration.toMinutesPart());
	}

	/**
	 * {@return true if there are no plays in this year}
	 */
	public boolean empty() {
		return this.totalDuration.isZero() || this.numScrobbles == 0;
	}
}
