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
package scrobbles4j.server.config;

import java.net.http.HttpClient;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * Creates a global instance of {@link java.net.http.HttpClient} configured to use the Quarkus' managed executor.
 *
 * @author Michael J. Simons
 */
public final class HttpClientProducer {

	/**
	 * @param managedExecutor A managed executor passed to the http client for executing asynchronous requests
	 * @return a HTTP client
	 */
	@Produces
	@Singleton
	public HttpClient jdbi(ManagedExecutor managedExecutor) {

		return HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.executor(managedExecutor)
			.build();
	}
}
