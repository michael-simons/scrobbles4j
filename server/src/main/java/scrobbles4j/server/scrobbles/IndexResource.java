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
package scrobbles4j.server.scrobbles;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

/**
 * Landing page.
 *
 * @author Michael J. Simons
 */
@Path("/")
public class IndexResource {

	private final ZoneId zoneId = ZoneId.systemDefault();

	private final ScrobbleService scrobbleService;

	private final Template index;

	/**
	 * Main entry.
	 * @param scrobbleService needed to access scrobbles
	 * @param index template for the main view
	 */
	@Inject
	public IndexResource(ScrobbleService scrobbleService, @Location("scrobbles/index") Template index) {

		this.scrobbleService = scrobbleService;
		this.index = index;
	}

	/**
	 * An overview about the recently played tracks.
	 * @param uriInfo needed to derive links
	 * @return a template instance
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index(@Context UriInfo uriInfo) {

		var cutOffDate = ZonedDateTime.now(this.zoneId).minusMonths(1).toInstant();
		return this.index.data("zone", this.zoneId)
			.data("locale", Locale.ENGLISH)
			.data("years", this.scrobbleService.getAvailableYears())
			.data("lastScrobbles", this.scrobbleService.getLatest(20, cutOffDate))
			.data("scrobbleStats", this.scrobbleService.getScrobbleStats());
	}

}
