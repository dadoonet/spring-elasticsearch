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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;

/**
 * An abstract {@link FactoryBean} used to create an ElasticSearch object.
 * @author David Pilato
 */
public abstract class ElasticsearchAbstractFactoryBean {

    protected Properties properties;

	protected boolean async = false;

	ThreadPoolTaskExecutor taskExecutor;

    /**
     * Elasticsearch properties
     * <p>Example:</p>
     * <pre>
	 * {@code
     *   <util:map id="esproperties">
     *     <entry key="cluster.name" value="newclustername"/>
     *   </util:map>
     *
     * <bean id="esClient" class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
     *   <property name="properties" ref="esproperties" />
     * </bean>
	 * }
     * </pre>
     * <p>Example:</p>
     * <pre>
     * {@code
     *   <util:properties id="esproperties" location="classpath:fr/pilato/spring/elasticsearch/xml/esnode-transport.properties"/>
     *   <elasticsearch:node id="esNode" properties="esproperties" />
     * }
     * </pre>
     * @param properties the properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
	}

	/**
	 * Enable async initialization
	 */
	public void setAsync(boolean async) {
		this.async = async;
	}

	/**
	 * Executor for async init mode
	 */
	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
    }
}
