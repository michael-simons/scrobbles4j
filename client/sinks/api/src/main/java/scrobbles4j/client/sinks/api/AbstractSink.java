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
package scrobbles4j.client.sinks.api;

/**
 * A base class for {@link Sink sinks} providing utility functions and possible state.
 *
 * @author Michael J. Simons
 */
public abstract class AbstractSink implements Sink {

	/**
	 * Useful for checks if this concrete sink has been initialized.
	 */
	private final Object lock;

	/**
	 * State whether this instance has been initialized or not.
	 */
	private boolean initialized;

	/**
	 * Public constructor required by the Java Service API. Also marks this instance as
	 * not initialized.
	 */
	protected AbstractSink() {
		this.lock = new Object();
		this.initialized = false;
	}

	/**
	 * {@return whether the sink has been initialized or not}
	 */
	protected final boolean isInitialized() {

		synchronized (this.lock) {
			return this.initialized;
		}
	}

	/**
	 * Run the initializer inside a lock.
	 * @param initializer the initializer to run
	 * @throws IllegalStateException if the sink has already been initialized
	 */
	protected final void doInitialize(Runnable initializer) {

		synchronized (this.lock) {
			if (this.initialized) {
				throw new IllegalStateException("Sink has been already initialized");
			}
			initializer.run();
			this.initialized = true;
		}
	}

}
