import scrobbles4j.client.sources.apple.music.SourceImpl;

/**
 * Implementation of the sources API via Apple Music and <a href="https://github.com/hendriks73/obstmusic">Obstmusic</a>.
 *
 * @author Michael J. Simons
 */
module scrobbles4j.client.sources.apple.music {

	provides scrobbles4j.client.sources.api.Source with SourceImpl;

	requires scrobbles4j.client.sources.api;
	requires tagtraum.obstmusic;
}