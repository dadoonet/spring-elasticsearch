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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.SSLUtils;
import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static fr.pilato.spring.elasticsearch.it.BaseTest.*;

public abstract class RestAppConfig {

	abstract protected void enrichFactory(ElasticsearchClientFactoryBean factory);

	@Bean
	public ElasticsearchClient esClient() throws Exception {
		ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
		enrichFactoryWithNodeSettings(factory);
		enrichFactory(factory);
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	public static void enrichFactoryWithNodeSettings(ElasticsearchClientFactoryBean factory) {
		factory.setEsNodes(List.of(HttpHost.create(testCluster)));
		factory.setUsername(testClusterUser);
		factory.setPassword(testClusterPass);
		if (testCluster.equals(DEFAULT_TEST_CLUSTER)) {
			factory.setSslContext(SSLUtils.yesSSLContext());
		}
	}
}
