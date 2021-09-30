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
package scrobbles4j.server.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

/**
 * Creates a global instance of {@link Jdbi} based on the one {@link DataSource}.
 *
 * @author Michael J. Simons
 */
public final class JdbiProducer {

	/**
	 * @param dataSource The datasource for which Jdbi should be produced
	 * @return a Jdbi instance
	 */
	@Produces
	@ApplicationScoped
	public Jdbi jdbi(DataSource dataSource) {
		return Jdbi.create(dataSource);
	}
}
