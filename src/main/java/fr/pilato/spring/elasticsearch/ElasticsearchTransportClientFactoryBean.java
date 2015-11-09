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
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An {@link FactoryBean} used to create an ElasticSearch Transport {@link Client}.
 * <br>
 * You need to define the nodes you want to communicate with.<br>
 * Don't forget to create an es.properties file if you want to set specific values
 * for this client, e.g.: cluster.name
 * <br>Example :
 * <pre>
 * {@code
 *  <bean id="esClient"
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
 *    <property name="esNodes">
 *      <list>
 *        <value>localhost:9300</value>
 *        <value>localhost:9301</value>
 *      </list>
 *    </property>
 *  </bean>
 * }
 * </pre>
 * @see TransportClient
 * @author David Pilato
 */
public class ElasticsearchTransportClientFactoryBean extends ElasticsearchAbstractClientFactoryBean {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String[] esNodes =  { "localhost:9300" };

	/**
	 * Define ES nodes to communicate with.
	 * @return An array of nodes hostname:port
	 */
	public String[] getEsNodes() {
		return esNodes;
	}

	/**
	 * Define ES nodes to communicate with.
	 * <br>use : hostname:port form
	 * <p>Example :</p>
 	 * <pre>
	 * {@code
	 * <property name="esNodes">
     *  <list>
     *   <value>localhost:9300</value>
     *   <value>localhost:9301</value>
     *  </list>
     * </property>
	 * }
	 * </pre>
	 * If not set, default to [ "localhost:9300" ].
	 * <br>If port is not set, default to 9300.
	 * @param esNodes An array of nodes hostname:port
	 */
	public void setEsNodes(String[] esNodes) {
		this.esNodes = esNodes;
	}

	@Override
	protected Client buildClient() throws Exception {
		Settings.Builder builder = Settings.builder();

        if (null != this.settings && null == properties) {
            builder.put(this.settings);
        }

        if (null != this.settingsFile && null == properties) {
            logger.warn("settings has been deprecated in favor of properties. See issue #15: https://github.com/dadoonet/spring-elasticsearch/issues/15.");
			builder.loadFromStream(settingsFile, ElasticsearchTransportClientFactoryBean.class.getResourceAsStream("/" + settingsFile));
        }

        if (null != this.properties) {
            builder.put(this.properties);
        }

		TransportClient client = TransportClient.builder()
				.settings(builder.build())
				.build();

		for (int i = 0; i < esNodes.length; i++) {
			client.addTransportAddress(toAddress(esNodes[i]));
		}

		return client;
	}

	/**
	 * Helper to define an hostname and port with a String like hostname:port
	 * @param address Node address hostname:port (or hostname)
	 * @return
	 */
	private InetSocketTransportAddress toAddress(String address) throws UnknownHostException {
		if (address == null) return null;

		String[] splitted = address.split(":");
		int port = 9300;
		if (splitted.length > 1) {
			port = Integer.parseInt(splitted[1]);
		}

		return new InetSocketTransportAddress(InetAddress.getByName(splitted[0]), port);
	}

}
