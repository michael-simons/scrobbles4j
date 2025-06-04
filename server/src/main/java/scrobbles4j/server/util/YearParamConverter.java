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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Year;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/**
 * Allows using {@link Year} objects as parameters in resources.
 *
 * @author Michael J. Simons
 */
public final class YearParamConverter implements ParamConverter<Year> {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");

	private YearParamConverter() {
	}

	@Override
	public Year fromString(String value) {
		if (value == null) {
			return null;
		}

		return this.formatter.parse(value, Year::from);
	}

	@Override
	public String toString(Year value) {
		if (value == null) {
			return null;
		}
		return this.formatter.format(value);
	}

	/**
	 * Provider for the {@link YearParamConverter}. Always returns the same instance.
	 */
	@Provider
	public static class YearParamConverterProvider implements ParamConverterProvider {

		private final YearParamConverter instance = new YearParamConverter();

		@SuppressWarnings("unchecked")
		@Override
		public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

			if (rawType.equals(Year.class)) {
				return (ParamConverter<T>) this.instance;
			}
			return null;
		}

	}

}
