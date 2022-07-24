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

import java.util.Comparator;

/**
 * A comparator for {@link Sink sinks}, comparing them by their {@link Sink#getOrder()}, in which higher values mean
 * precedence.
 *
 * @author Michael J. Simons
 */
public enum SinkComparator implements Comparator<Sink> {

	/**
	 * The single instance of the {@link SinkComparator}.
	 */
	INSTANCE;

	@Override
	public int compare(Sink o1, Sink o2) {
		return -Integer.compare(o1.getOrder(), o2.getOrder());
	}
}
