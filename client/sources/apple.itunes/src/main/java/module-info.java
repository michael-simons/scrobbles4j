import scrobbles4j.client.sources.apple.itunes.SourceImpl;

/**
 * Implementation of the sources API via Apple Music and <a href="https://github.com/hendriks73/obstunes">Obstunes</a>.
 *
 * @author Michael J. Simons
 */
module scrobbles4j.client.sources.apple.itunes {

	provides scrobbles4j.client.sources.api.Source with SourceImpl;

	requires scrobbles4j.client.sources.api;
	requires tagtraum.obstunes;
}