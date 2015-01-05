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

package fr.pilato.spring.elasticsearch.xml;

import fr.pilato.spring.elasticsearch.BaseTest;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;


/**
 * We try to merge non merging settings.
 * An exception should be raised.
 * @author David Pilato aka dadoonet
 *
 */
public class SettingsFailedTest extends BaseTest {
	static protected ConfigurableApplicationContext ctx;

	@Test(expected=BeanCreationException.class)
	public void test_merge_settings_failure() {
		try {
			new ClassPathXmlApplicationContext("models/settings-failed/settings-failed-context.xml");
		} catch (BeanCreationException e) {
			assertEquals(ElasticsearchIllegalArgumentException.class, e.getCause().getClass());
			throw e;
		}
	}
}
