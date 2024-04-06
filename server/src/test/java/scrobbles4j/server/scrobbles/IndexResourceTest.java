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
package scrobbles4j.server.scrobbles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(IndexResource.class)
class IndexResourceTest {

	@Test
	void indexShouldWork() {
		var response = RestAssured.when().get()
			.then().statusCode(200)
			.extract().htmlPath();

		var title = response
			.getString("html.body.main.ol.li[0].span[0]");
		assertThat(title).isEqualTo("\"Only For The Weak\"");
		var playedOn = response
			.getString("html.body.main.ol.li[0].span[3]");

		var time = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).plus(Duration.ofMinutes(123));
		var formatter = DateTimeFormatter.ofPattern("HH:mm");
		assertThat(playedOn).endsWith(formatter.format(time.atZone(ZoneId.systemDefault())));
	}
}
