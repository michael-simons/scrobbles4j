/*
 * Copyright 2021-2024 the original author or authors.
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
package scrobbles4j.server.util;

import io.quarkus.qute.TemplateExtension;

/**
 * Generates links to Apples Shortcuts application, running the shortcuts with the same name. See
 * <a href="https://support.apple.com/de-de/guide/shortcuts-mac/apd624386f42/mac">Kurzbefehl mit einer URL ausführen</a>.
 *
 * @author Michael J. Simons
 */
@TemplateExtension(namespace = "sc")
public final class Shortcuts {

	static String pushTrack(String artist, String name) {
		return "shortcuts://run-shortcut?name=Push%%20track&input=text&text=%s%%0A%s".formatted(
			StrExtensions.urlEncode(artist), StrExtensions.urlEncode(name)
		);
	}

	static String pushAlbum(String artist, String name) {
		return "shortcuts://run-shortcut?name=Push%%20album&input=text&text=%s%%0A%s".formatted(
			StrExtensions.urlEncode(artist), StrExtensions.urlEncode(name)
		);
	}

	static String queueTrack(String artist, String name) {
		return "shortcuts://run-shortcut?name=Queue%%20track&input=text&text=%s%%0A%s".formatted(
			StrExtensions.urlEncode(artist), StrExtensions.urlEncode(name)
		);
	}

	private Shortcuts() {
	}
}
