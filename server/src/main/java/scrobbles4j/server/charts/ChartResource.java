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
import scrobbles4j.model.Artist;

import java.time.Year;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Returning charts.
 *
 * @author Michael J. Simons
 */
@Path("/charts")
public class ChartResource {

	private final ChartService chartService;

	private final Template indexTemplate;

	private final Template yearTemplate;

	private final Template artistTemplate;

	@Inject
	public ChartResource(
		ChartService chartService,
		@Location("charts/index") Template indexTemplate,
		@Location("charts/year") Template yearTemplate,
		@Location("charts/artist") Template artistTemplate
	) {
		this.chartService = chartService;
		this.indexTemplate = indexTemplate;
		this.yearTemplate = yearTemplate;
		this.artistTemplate = artistTemplate;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index() {

		return indexTemplate
			.data("topTracks", this.chartService.getTopNTracks(10, Optional.empty(), Optional.empty()))
			.data("topAlbums", this.chartService.getTopNAlbums(10, Optional.empty()))
			.data("favoriteArtists", this.chartService.getFavoriteArtistsByYears(5, 10));
	}

	@GET
	@Path("/{year: (\\d+)}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance year(@PathParam("year") Year year) {

		return this.yearTemplate
			.data("year", year)
			.data("scrobbleStats", this.chartService.getStats(year))
			.data("topTracks", this.chartService.getTopNTracks(5, Optional.of(year), Optional.empty()))
			.data("topAlbums", this.chartService.getTopNAlbums(10, Optional.of(year)))
			.data("topArtists", this.chartService.getTopNArtists(10, year));
	}

	@GET
	@Path("/artist")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance artist(@QueryParam("q") String q) {

		var artist = Optional.ofNullable(q).map(String::trim).filter(Predicate.not(String::isBlank)).map(Artist::new)
			.orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Query parameter is mandatory.").build()));

		var topTracks = this.chartService.getTopNTracks(20, Optional.empty(), Optional.of(artist));
		if (topTracks.isEmpty()) {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No such artist.").build());
		}

		return this.artistTemplate
			.data("artist", topTracks.stream().findFirst().map(t -> new Artist(t.artist())).get())
			.data("topTracks", topTracks);
	}
}
