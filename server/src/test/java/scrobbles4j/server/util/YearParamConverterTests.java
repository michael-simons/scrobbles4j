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
package scrobbles4j.server.util;

import java.time.Year;
import java.time.temporal.Temporal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michael J. Simons
 */
class YearParamConverterTests {

	private final YearParamConverter.YearParamConverterProvider provider = new YearParamConverter.YearParamConverterProvider();

	@Test
	void shouldBeAbleToConvertYears() {

		var converter = this.provider.getConverter(Year.class, null, null);
		assertThat(converter).isNotNull();
	}

	@Test
	void shouldBNoteAbleToConvertThings() {

		var converter = this.provider.getConverter(Object.class, null, null);
		assertThat(converter).isNull();
	}

	@Test
	void shouldDealWithNullString() {

		var converter = this.provider.getConverter(Year.class, null, null);
		assertThat((Temporal) converter.fromString(null)).isNull();
	}

	@Test
	void shouldDealWithNullValues() {

		var converter = this.provider.getConverter(Year.class, null, null);
		assertThat(converter.toString(null)).isNull();
	}

	@Test
	void shouldConvertStrings() {

		var converter = this.provider.getConverter(Year.class, null, null);
		assertThat((Temporal) converter.fromString("2021")).isEqualTo(Year.of(2021));
	}

	@Test
	void shouldConvertYears() {

		var converter = this.provider.getConverter(Year.class, null, null);
		assertThat(converter.toString(Year.of(2021))).isEqualTo("2021");
	}

}
