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
package scrobbles4j.server.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Prints the rank or omits it if the previous one is the same.
 *
 * @author Michael J. Simons
 */
public final class RankFormatter {

	private final AtomicInteger previous = new AtomicInteger();

	private RankFormatter() {
	}

	/**
	 * Return the current rank if the current rank is higher than the previous one,
	 * otherwise returns an empty string.
	 * @param rank the current rank
	 * @return a string with the rank if it changed
	 */
	public String format(int rank) {

		if (this.previous.compareAndSet(rank - 1, rank)) {
			return Integer.toString(rank);
		}
		return "";
	}

	/**
	 * A supplier that returns a new formatter on each call.
	 */
	@Named("rankFormatter")
	@Singleton
	public static class RankFormatterSupplier implements Supplier<RankFormatter> {

		/**
		 * This constructor is required for CDI to work.
		 */
		public RankFormatterSupplier() {
		}

		@Override
		public RankFormatter get() {
			return new RankFormatter();
		}

	}

}
