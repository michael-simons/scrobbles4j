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

/**
 * A marker interface for stats that have a duration.
 *
 * @author Michael J. Simons
 */
public interface HasDuration {

	/**
	 * {@return a formatted, total duration (in days, hours and minutes) of playtime}
	 */
	default String formattedDuration() {
		if (duration().isZero()) {
			return "";
		}

		var days = duration().toDaysPart();
		var hours = duration().toHoursPart();
		var minutes = duration().toMinutesPart();
		return String.format("%d day%s, %d hour%s and %d minute%s", days, plural(days), hours, plural(hours), minutes,
				plural(minutes));
	}

	private String plural(long value) {
		return (value > 1) ? "s" : "";
	}

	/**
	 * {@return the total duration of tracks played in this period}
	 */
	Duration duration();

}
