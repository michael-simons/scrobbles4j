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
package scrobbles4j.server.charts;

import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.virtual.threads.VirtualThreads;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scrobbles4j.server.model.Albums;
import scrobbles4j.server.model.Artists;
import scrobbles4j.server.model.WikimediaImage;

/**
 * Returning charts.
 *
 * @author Michael J. Simons
 */
@Path("/charts")
public class ChartResource {

	private final ExecutorService executorService;

	private final ChartService chartService;

	private final Artists artists;

	private final Albums albums;

	private final Template indexTemplate;

	private final Template yearTemplate;

	private final Template monthTemplate;

	private final Template artistTemplate;

	/**
	 * Create a new instance of this resource.
	 * @param executorService needed for a couple of database requests
	 * @param chartService access to charts
	 * @param artists access to artists
	 * @param albums access to albums
	 * @param indexTemplate the view for the main entry
	 * @param yearTemplate the year view
	 * @param monthTemplate the view for the month
	 * @param artistTemplate the artist view
	 */
	@Inject
	public ChartResource(@VirtualThreads ExecutorService executorService, ChartService chartService, Artists artists,
			Albums albums, @Location("charts/index") Template indexTemplate,
			@Location("charts/year") Template yearTemplate, @Location("charts/month") Template monthTemplate,
			@Location("charts/artist") Template artistTemplate) {
		this.executorService = executorService;
		this.chartService = chartService;
		this.artists = artists;
		this.albums = albums;

		this.indexTemplate = indexTemplate;
		this.yearTemplate = yearTemplate;
		this.monthTemplate = monthTemplate;
		this.artistTemplate = artistTemplate;
	}

	/**
	 * Chart index page.
	 * @param includeCompilations flag if compilations should be included while computing
	 * the track list
	 * @return charts overview
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index(
			@QueryParam("includeCompilations") @DefaultValue("false") boolean includeCompilations) {

		var numArtists = 10;
		return this.indexTemplate.data("includeCompilations", includeCompilations)
			.data("topTracks",
					this.chartService.getTopNTracks(10, Optional.empty(), Optional.empty(), includeCompilations))
			.data("topAlbums", this.chartService.getTopNAlbums(10, Optional.empty()))
			.data("numArtists", numArtists)
			.data("topArtistsByYear", this.chartService.getTopNArtistsByYear(numArtists, 10))
			.data("topArtists", this.chartService.getTopNArtists(numArtists));
	}

	/**
	 * Charts per year.
	 * @param year the year in question
	 * @param includeCompilations flag if compilations should be included while computing
	 * the track list
	 * @return a view
	 */
	@GET
	@Path("/{year: (\\d+)}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance year(@PathParam("year") Year year,
			@QueryParam("includeCompilations") @DefaultValue("true") boolean includeCompilations) {

		var maxMonth = YearMonth.now();
		var months = new ArrayList<YearMonth>();
		var month = year.atMonth(1);
		while (month.isBefore(maxMonth)) {
			months.add(month);
			if (month.getMonthValue() == 12) {
				break;
			}
			month = month.plusMonths(1);
		}

		return this.yearTemplate.data("year", year)
			.data("includeCompilations", includeCompilations)
			.data("scrobbleStats", this.chartService.getStats(year))
			.data("months", months)
			.data("locale", Locale.ENGLISH)
			.data("topTracks",
					this.chartService.getTopNTracks(5, Optional.of(year), Optional.empty(), includeCompilations))
			.data("topNewTracks", this.chartService.getTopNNewTracksInYear(5, year))
			.data("topAlbums", this.chartService.getTopNAlbums(10, Optional.of(year)))
			.data("topArtists", this.chartService.getTopNArtists(10, year));
	}

	/**
	 * A monthly recap with the top 5 artists and albums.
	 * @param year the year
	 * @param month the month
	 * @return a view
	 */
	@GET
	@Path("/{year: (\\d+)}/{month: (\\d+)}")
	@Produces(MediaType.TEXT_HTML)
	@RunOnVirtualThread
	public TemplateInstance month(@PathParam("year") Year year, @Min(1) @Max(12) @PathParam("month") int month) {

		record NameAndImage(String name, Optional<WikimediaImage> image) {
		}

		var yearMonth = year.atMonth(month);
		var summary = CompletableFuture.supplyAsync(() -> this.chartService.getStats(yearMonth), this.executorService);

		var top5Artists = CompletableFuture
			.supplyAsync(() -> this.chartService.getTopNArtists(5, yearMonth), this.executorService)
			.thenApply(rankedEntries -> rankedEntries.stream()
				.map(entry -> new RankedEntry<>(entry.rank(), entry.cnt(),
						new NameAndImage(entry.value().name(), this.artists.getImage(entry.value()))))
				.toList());

		var top1Tracks = CompletableFuture.supplyAsync(() -> this.chartService.getTop1Tracks(yearMonth),
				this.executorService);

		var top5Albums = CompletableFuture.supplyAsync(() -> this.chartService.getTopNAlbums(5, yearMonth),
				this.executorService);

		return this.monthTemplate.data("summary", summary)
			.data("locale", Locale.ENGLISH)
			.data("tracks", top1Tracks)
			.data("artists", top5Artists)
			.data("albums", top5Albums);
	}

	/**
	 * Charts per artist.
	 * @param artistName required query parameter for selecting the artist
	 * @param includeCompilations flag if compilations should be included while computing
	 * the track list
	 * @return a view
	 */
	@GET
	@Path("/artist")
	@Produces(MediaType.TEXT_HTML)
	@RunOnVirtualThread
	public TemplateInstance artist(@NotNull @NotBlank @QueryParam("q") String artistName,
			@QueryParam("includeCompilations") @DefaultValue("true") boolean includeCompilations) {

		var artist = this.artists.findByName(artistName)
			.orElseThrow(() -> new WebApplicationException(
					Response.status(Response.Status.NOT_FOUND).entity("No such artist.").build()));

		var summary = CompletableFuture.supplyAsync(() -> this.artists.getSummary(artist), this.executorService);

		var topTracks = CompletableFuture.supplyAsync(
				() -> this.chartService.getTopNTracks(20, Optional.empty(), Optional.of(artist), includeCompilations),
				this.executorService);

		var albumsByArtists = CompletableFuture.supplyAsync(() -> this.albums.findByArtist(artist),
				this.executorService);

		var relatedArtists = CompletableFuture.supplyAsync(() -> this.artists.findRelated(artist),
				this.executorService);

		return this.artistTemplate.data("includeCompilations", includeCompilations)
			.data("artist", artist)
			.data("summary", summary)
			.data("image", this.artists.getImage(artist))
			.data("topTracks", topTracks)
			.data("albumsByArtists", albumsByArtists)
			.data("relatedArtists", relatedArtists);
	}

}
