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
import scrobbles4j.server.model.Albums;
import scrobbles4j.server.model.Artists;

import java.time.Year;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * Returning charts.
 *
 * @author Michael J. Simons
 */
@Path("/charts")
public class ChartResource {

	private final ManagedExecutor managedExecutor;

	private final ChartService chartService;

	private final Artists artists;

	private final Albums albums;

	private final Template indexTemplate;

	private final Template yearTemplate;

	private final Template artistTemplate;

	/**
	 * Create a new instance of this resource
	 *
	 * @param managedExecutor Needed for a couple of database requests
	 * @param chartService    Access to charts
	 * @param artists         Access to artists
	 * @param albums          Access to albums
	 * @param indexTemplate   The view for the main entry
	 * @param yearTemplate    The year view
	 * @param artistTemplate  The artist view
	 */
	@Inject
	public ChartResource(
		ManagedExecutor managedExecutor,
		ChartService chartService,
		Artists artists,
		Albums albums,
		@Location("charts/index") Template indexTemplate,
		@Location("charts/year") Template yearTemplate,
		@Location("charts/artist") Template artistTemplate
	) {
		this.managedExecutor = managedExecutor;
		this.chartService = chartService;
		this.artists = artists;
		this.albums = albums;

		this.indexTemplate = indexTemplate;
		this.yearTemplate = yearTemplate;
		this.artistTemplate = artistTemplate;
	}

	/**
	 * @param includeCompilations Flag if compilations should be included while computing the track list
	 * @return charts overview
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index(@QueryParam("includeCompilations") @DefaultValue("false") boolean includeCompilations) {

		var numArtists = 10;
		return indexTemplate
			.data("includeCompilations", includeCompilations)
			.data("topTracks", this.chartService.getTopNTracks(10, Optional.empty(), Optional.empty(), includeCompilations))
			.data("topAlbums", this.chartService.getTopNAlbums(10, Optional.empty()))
			.data("numArtists", numArtists)
			.data("favoriteArtists", this.chartService.getFavoriteArtistsByYears(numArtists, 10));
	}

	/**
	 * Charts per year
	 *
	 * @param year                The year in question
	 * @param includeCompilations Flag if compilations should be included while computing the track list
	 * @return A view
	 */
	@GET
	@Path("/{year: (\\d+)}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance year(
		@PathParam("year") Year year,
		@QueryParam("includeCompilations") @DefaultValue("true") boolean includeCompilations
	) {

		return this.yearTemplate
			.data("year", year)
			.data("includeCompilations", includeCompilations)
			.data("scrobbleStats", this.chartService.getStats(year))
			.data("topTracks", this.chartService.getTopNTracks(5, Optional.of(year), Optional.empty(), includeCompilations))
			.data("topNewTracks", this.chartService.getTopNNewTracksInYear(5, year))
			.data("topAlbums", this.chartService.getTopNAlbums(10, Optional.of(year)))
			.data("topArtists", this.chartService.getTopNArtists(10, year));
	}

	/**
	 * Charts per artist
	 *
	 * @param q                   Required query paramter for selecting the artist
	 * @param includeCompilations Flag if compilations should be included while computing the track list
	 * @return A view
	 */
	@GET
	@Path("/artist")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance artist(@QueryParam("q") String q, @QueryParam("includeCompilations") @DefaultValue("true") boolean includeCompilations) {

		var artistName = Optional.ofNullable(q).map(String::trim).filter(Predicate.not(String::isBlank))
			.orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Query parameter is mandatory.").build()));

		var artist = artists.findByName(artistName)
			.orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No such artist.").build()));

		var topTracks = CompletableFuture
			.supplyAsync(() -> this.chartService.getTopNTracks(20, Optional.empty(), Optional.of(artist), includeCompilations), managedExecutor);

		var albumsByArtists = CompletableFuture
			.supplyAsync(() -> this.albums.findByArtist(artist), managedExecutor);

		var relatedArtists = CompletableFuture
			.supplyAsync(() -> this.artists.findRelated(artist), managedExecutor);

		return this.artistTemplate
			.data("includeCompilations", includeCompilations)
			.data("artist", artist)
			.data("topTracks", topTracks)
			.data("albumsByArtists", albumsByArtists)
			.data("relatedArtists", relatedArtists);
	}
}
