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
package scrobbles4j.server.charts;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/charts")
public class ChartResource {

	private final ChartService chartService;

	private final Template index;

	private final Template overview;

	@Inject
	public ChartResource(
		ChartService chartService,
		@Location("charts/index") Template index,
		@Location("charts/overview") Template overview
	) {
		this.chartService = chartService;
		this.index = index;
		this.overview = overview;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index() {

		return index
			.data("topTracks", this.chartService.getTopNTracks(10))
			.data("favoriteArtists", this.chartService.getFavoriteArtistsByYears(5, 10));
	}

	@GET
	@Path("/{year: (\\d+)}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance year(
		@PathParam("year") int year
	) {
		return overview
			.data("year", year)
			.data("topTracks", this.chartService.getTopNTracks(5, Optional.of(year)))
			.data("topAlbums", this.chartService.getTopNAlbums(10, year))
			.data("topArtists", this.chartService.getTopNArtists(10, year));
	}
}
