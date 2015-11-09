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

package fr.pilato.spring.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link FactoryBean} implementation used to create a {@link Client} element
 * from a {@link Node}.
 * @author David Pilato
 * @see ElasticsearchAbstractClientFactoryBean
 * @see ElasticsearchNodeFactoryBean
 */
public class ElasticsearchClientFactoryBean extends ElasticsearchAbstractClientFactoryBean {

	@Autowired Node node;

	/**
	 * Node to get a client from
	 * @param node An Elasticsearch node
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	protected Client buildClient() throws Exception {
		if (node == null)
			throw new Exception(
					"You must define an ElasticSearch Node as a Spring Bean.");
		return node.client();
	}
}
