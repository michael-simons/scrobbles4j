/**
 * The source API that various integrations need to implement to be included as source for the Scrobbles4j client.
 *
 * @author Michael J. Simons
 */
module scrobbles4j.client.sources.api {
	requires transitive scrobbles4j.model;

	exports scrobbles4j.client.sources.api;
}