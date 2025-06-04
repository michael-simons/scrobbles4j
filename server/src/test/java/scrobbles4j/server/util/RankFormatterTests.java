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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michael J. Simons
 */
class RankFormatterTests {

	private final RankFormatter.RankFormatterSupplier supplier = new RankFormatter.RankFormatterSupplier();

	@Test
	void shouldOnlyChangeOnDifferentSuccessiveValues() {

		var formatter = this.supplier.get();

		assertThat(formatter.format(0)).isEqualTo("");

		assertThat(formatter.format(1)).isEqualTo("1");
		assertThat(formatter.format(1)).isEqualTo("");

		assertThat(formatter.format(2)).isEqualTo("2");

		assertThat(formatter.format(23)).isEqualTo("");
	}

	@Nested
	class RankFormatterSupplierTest {

		@Test
		void supplierShouldGiveOutFreshFormatters() {
			var f1 = RankFormatterTests.this.supplier.get();
			var f2 = RankFormatterTests.this.supplier.get();
			assertThat(f1).isNotSameAs(f2);
		}

	}

}
