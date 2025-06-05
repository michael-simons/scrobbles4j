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
import java.time.YearMonth;

/**
 * Monthly statistics.
 *
 * @author Michael J. Simons
 * @param month the month
 * @param numScrobbles number of tracks played in the given year
 * @param duration total time
 */
public record MonthStats(YearMonth month, int numScrobbles, Duration duration) implements HasDuration {

	/**
	 * {@return the previous month}
	 */
	public YearMonth previous() {
		return this.month.minusMonths(1);
	}

	/**
	 * {@return the next month}
	 */
	public YearMonth next() {
		return this.month.plusMonths(1);
	}

	/**
	 * {@return true if there are no plays in this month}
	 */
	public boolean empty() {
		return this.duration.isZero() || this.numScrobbles == 0;
	}
}
