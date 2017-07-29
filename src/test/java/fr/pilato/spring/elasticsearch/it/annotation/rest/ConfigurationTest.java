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

package fr.pilato.spring.elasticsearch.it.annotation.rest;

import fr.pilato.spring.elasticsearch.it.BaseTest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:annotation-rest-context.xml"
		})
public class ConfigurationTest extends BaseTest {

	@Autowired
	private RestClient client;

	@Autowired
	private RestHighLevelClient highLevelClient;

	@Test
	public void testClient() throws IOException {
		assertNotNull(client);
		assertNotNull(highLevelClient);

		// We call the High Level Rest client to make sure it works properly
        highLevelClient.info();
    }

	@After
	public void tearDown() throws IOException {
		if (client != null) {
			client.close();
		}
	}
}
