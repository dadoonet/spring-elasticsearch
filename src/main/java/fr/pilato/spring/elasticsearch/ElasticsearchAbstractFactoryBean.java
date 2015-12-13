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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.Properties;

/**
 * An abstract {@link FactoryBean} used to create an ElasticSearch object.
 * @author David Pilato
 */
public abstract class ElasticsearchAbstractFactoryBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String settingsFile = null;
	
	protected Map<String, String> settings;

    protected Properties properties;

	protected boolean async = false;

	protected ThreadPoolTaskExecutor taskExecutor;

	/**
	 * Elasticsearch Settings file classpath URL (default : es.properties)
	 * <p>Example :</p>
 	 * <pre>
	 * {@code
	 * <property name="settingsFile" value="es.properties" />
	 * }
	 * </pre>
	 * @param settingsFile the settingsFile to set
	 * @see #setSettings(Map) to define settings in spring xml file
     * @deprecated by {@link #setProperties(java.util.Properties)}
	 */
    @Deprecated
	public void setSettingsFile(String settingsFile) {
        logger.warn("settingsFile has been deprecated in favor of properties. See issue #15: https://github.com/dadoonet/spring-elasticsearch/issues/15." +
                "\n You should now write something like: <util:properties id=\"esproperties\" location=\"classpath:" + settingsFile + "\"/>");
		this.settingsFile = settingsFile;
	}

	/**
	 * Embedded Elasticsearch settings
	 * <p>Example :</p>
	 * 
	 * @param settings
	 * @see #setSettingsFile(String) to define settings in file
     * @deprecated by {@link #setProperties(java.util.Properties)}
	 */
    @Deprecated
	public void setSettings(final Map<String, String> settings) {
        logger.warn("settings has been deprecated in favor of properties. See issue #15: https://github.com/dadoonet/spring-elasticsearch/issues/15.");
		this.settings = settings;
	}

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
	 *
	 * @param async
	 */
	public void setAsync(boolean async) {
		this.async = async;
	}

	/**
	 * Executor for async init mode
	 *
	 * @param taskExecutor
	 */
	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
    }
}
