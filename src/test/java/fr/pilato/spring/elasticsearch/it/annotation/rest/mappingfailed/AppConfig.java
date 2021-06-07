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

package fr.pilato.spring.elasticsearch.it.annotation.rest.mappingfailed;

import fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AppConfig {

	@Bean
	public RestHighLevelClient esClient(Properties esProperties) throws Exception {
		ElasticsearchRestClientFactoryBean factory = new ElasticsearchRestClientFactoryBean();
		factory.setProperties(esProperties);
		factory.setClasspathRoot("/models/root/mapping-failed/client1");
		factory.setMappings(new String[]{"twitter"});
		factory.setForceMapping(false);
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public RestHighLevelClient esClient2(Properties esProperties) throws Exception {
		ElasticsearchRestClientFactoryBean factory = new ElasticsearchRestClientFactoryBean();
		factory.setProperties(esProperties);
		factory.setClasspathRoot("/models/root/mapping-failed/client2");
		factory.setMappings(new String[]{"twitter"});
		factory.setMergeMapping(true);
		factory.afterPropertiesSet();
		return factory.getObject();
	}
}
