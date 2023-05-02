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
package scrobbles4j.server.scrobbles;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

/**
 * @author Michael J. Simons
 */
@Path("/")
public class IndexResource {

	private final ZoneId zoneId = ZoneId.systemDefault();

	private final ScrobbleService scrobbleService;

	private final Template index;

	/**
	 * @param scrobbleService Needed to access scrobbles
	 * @param index           Template for the main view
	 */
	@Inject
	public IndexResource(ScrobbleService scrobbleService, @Location("scrobbles/index") Template index) {

		this.scrobbleService = scrobbleService;
		this.index = index;
	}

	/**
	 * An overview about the recently played tracks
	 *
	 * @param uriInfo Needed to derive links
	 * @return A template instance
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index(@Context UriInfo uriInfo) {

		var cutOffDate = ZonedDateTime.now(zoneId).minusMonths(1).toInstant();
		return index
			.data("zone", zoneId)
			.data("locale", Locale.UK)
			.data("years", scrobbleService.getAvailableYears())
			.data("lastScrobbles", scrobbleService.getLatest(20, cutOffDate))
			.data("scrobbleStats", scrobbleService.getScrobbleStats());
	}
}
