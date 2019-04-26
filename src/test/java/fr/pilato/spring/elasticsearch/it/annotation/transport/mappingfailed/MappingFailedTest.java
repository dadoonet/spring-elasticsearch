/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.spring.elasticsearch.it.annotation.transport.mappingfailed;

import fr.pilato.spring.elasticsearch.it.BaseTest;
import fr.pilato.spring.elasticsearch.it.annotation.SecurityOptionalConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * We try to merge non merging mapping.
 * An exception should be raised.
 * @author David Pilato aka dadoonet
 *
 */
public class MappingFailedTest extends BaseTest {

	@Override
	public String indexName() {
		return null;
	}

	@Test
	void test_transport_client() {
		assertThrows(BeanCreationException.class, () -> {
			try {
				logger.info("  --> Starting Spring Context on [{}] classpath", this.getClass().getPackage().getName());
				new AnnotationConfigApplicationContext(SecurityOptionalConfig.class, AppConfig.class);
			} catch (BeanCreationException e) {
				Throwable cause = e.getCause().getCause();
				assertEquals(IllegalArgumentException.class, cause.getClass());
				IllegalArgumentException responseException = (IllegalArgumentException) cause;
				assertThat(responseException.getMessage(), is("mapper [message] of different type, current_type [text], merged_type [date]"));
				throw e;
			}
		});
	}
}
