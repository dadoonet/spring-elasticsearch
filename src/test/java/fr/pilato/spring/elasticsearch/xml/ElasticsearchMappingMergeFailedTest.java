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

import static org.junit.Assert.*;

import org.elasticsearch.index.mapper.MergeMappingException;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * We try to merge non merging mapping.
 * An exception should be raised.
 * @author David Pilato aka dadoonet
 *
 */
public class ElasticsearchMappingMergeFailedTest {
	
	@Test(expected=BeanCreationException.class)
	public void test_transport_client() {
		try {
			new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-mapping-failed-test-context.xml");
		} catch (BeanCreationException e) {
			assertEquals(MergeMappingException.class, e.getCause().getClass());
			throw e;
		}
	}
}
