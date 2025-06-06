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
package scrobbles4j.server.config;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

import io.quarkus.virtual.threads.VirtualThreads;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * Creates a global instance of {@link java.net.http.HttpClient} configured to use the
 * Quarkus' managed executor.
 *
 * @author Michael J. Simons
 */
public final class HttpClientProducer {

	HttpClientProducer() {
	}

	/**
	 * Creates a new Http client using the same executor as Quarkus.
	 * @param executorService a managed executor passed to the http client for executing
	 * asynchronous requests
	 * @return a HTTP client
	 */
	@Produces
	@Singleton
	public HttpClient httpClient(@VirtualThreads ExecutorService executorService) {

		return HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.executor(executorService)
			.build();
	}

}
