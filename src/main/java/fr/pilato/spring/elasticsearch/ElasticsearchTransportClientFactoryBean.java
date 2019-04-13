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

import fr.pilato.spring.elasticsearch.proxy.GenericInvocationHandler;
import fr.pilato.spring.elasticsearch.util.Tuple;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static fr.pilato.elasticsearch.tools.alias.AliasElasticsearchUpdater.createAlias;
import static fr.pilato.elasticsearch.tools.index.IndexElasticsearchUpdater.createIndex;
import static fr.pilato.elasticsearch.tools.index.IndexElasticsearchUpdater.updateSettings;
import static fr.pilato.elasticsearch.tools.template.TemplateElasticsearchUpdater.createTemplate;
import static fr.pilato.spring.elasticsearch.type.TypeElasticsearchUpdater.createMapping;

/**
 * An abstract {@link FactoryBean} used to create an Elasticsearch
 * {@link Client}.
 * <p>
 * The lifecycle of the underlying {@link Client} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link Client#close()}
 * </p>
 * <p>
 * You need to define the nodes you want to communicate with.<br>
 * Don't forget to create an es.properties file if you want to set specific values
 * for this client, e.g.: cluster.name
 * <br>Example :
 * </p>
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
 * <p>
 * You can define properties for this bean that will help to auto create indexes
 * and types.
 * <br>
 * By default, the factory will load a es.properties file in the classloader. It will
 * contains all information needed for your client, e.g.: cluster.name
 * <br>
 * If you want to  modify the filename used for properties, just define the settingsFile property.
 * <p>In the following example, we will create two indexes :</p>
 * <ul>
 *   <li>twitter
 *   <li>rss
 * </ul>
 * twitter index will contain a type :
 * <ul>
 *   <li>_doc
 * </ul>
 * rss index will contain two types :
 * <ul>
 *   <li>_doc
 * </ul>
 * Then we will define an alias alltheworld for twitter and rss indexes.
 *
 * <pre>
 * {@code
 *  <bean id="esClient"
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
 *    <property name="mappings">
 *      <list>
 *        <value>twitter/_doc</value>
 *        <value>rss/_doc</value>
 *      </list>
 *    </property>
 *    <property name="aliases">
 *      <list>
 *        <value>alltheworld:twitter</value>
 *        <value>alltheworld:rss</value>
 *      </list>
 *    </property>
 *    <property name="templates">
 *      <list>
 *        <value>rss_template</value>
 *      </list>
 *    </property>
 *    <property name="forceMapping" value="false" />
 *    <property name="mergeMapping" value="true" />
 *    <property name="forceTemplate" value="false" />
 *    <property name="mergeSettings" value="true" />
 *    <property name="settingsFile" value="es.properties" />
 *    <property name="autoscan" value="false" />
 *  </bean>
 * }
 * </pre>
 *
 * By default, indexes are created with their default Elasticsearch settings. You can specify
 * your own settings for your index by putting a /es/indexname/_settings.json in your classpath.
 * <br>
 * So if you create a file named /es/twitter/_settings.json in your src/main/resources folder (for maven lovers),
 * it will be used by the factory to create the twitter index.
 * <pre>
 * {
 *   "index" : {
 *     "number_of_shards" : 3,
 *     "number_of_replicas" : 2
 *   }
 * }
 * </pre>
 * By default, types are not created and wait for the first document you send to Elasticsearch (auto mapping).
 * But, if you define a file named /es/indexname/_doc.json in your classpath, the _doc type will be created at startup using
 * the type definition you give.
 * <br>
 * So if you create a file named /es/twitter/_doc.json in your src/main/resources folder (for maven lovers),
 * it will be used by the factory to create the _doc type in twitter index.
 * <pre>
 * {
 *   "_doc" : {
 *     "properties" : {
 *       "message" : {"type" : "text"}
 *     }
 *   }
 * }
 * </pre>
 *
 * By convention, the factory will create all settings and mappings found under the /es classpath.<br>
 * You can disable convention and use configuration by setting autoscan to false.
 *
 * @see TransportClient
 * @see ElasticsearchRestClientFactoryBean
 * @author David Pilato
 * @deprecated Replaced by the RestClient implementation
 */
public class ElasticsearchTransportClientFactoryBean extends ElasticsearchAbstractFactoryBean
        implements FactoryBean<Client>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchTransportClientFactoryBean.class);

    private Client client;
    private Client proxyfiedClient;

    private boolean forceMapping;

    private boolean forceTemplate;

    private boolean mergeMapping;

    private boolean mergeSettings = true;

    private boolean autoscan = true;

    private String[] mappings;

    private String[] aliases;

    private String[] templates;

    private String classpathRoot = "es";

    /**
     * Set to true if you want to force reinit indexes/mapping
     * @param forceMapping true if you want to force reinit indexes/mapping
     */
    public void setForceMapping(boolean forceMapping) {
        this.forceMapping = forceMapping;
    }

    /**
     * Set to true if you want to force recreate templates
     * @param forceTemplate true if you want to force recreate templates
     */
    public void setForceTemplate(boolean forceTemplate) {
        this.forceTemplate = forceTemplate;
    }

    /**
     * Set to true if you want to try to merge mappings
     * @param mergeMapping true if you want to try to merge mappings
     */
    public void setMergeMapping(boolean mergeMapping) {
        this.mergeMapping = mergeMapping;
    }

    /**
     * Set to true if you want to try to merge index settings
     * @param mergeSettings true if you want to try to merge index settings
     */
    public void setMergeSettings(boolean mergeSettings) {
        this.mergeSettings = mergeSettings;
    }

    /**
     * Set to false if you want to use configuration instead of convention.
     * @param autoscan false if you want to use configuration instead of convention.
     */
    public void setAutoscan(boolean autoscan) {
        this.autoscan = autoscan;
    }


    /**
     * Define mappings you want to manage with this factory
     * <br>use : indexname/_doc form
     * <p>Example :</p>
     * <pre>
     * {@code
     * <property name="mappings">
     *  <list>
     *   <value>twitter/_doc</value>
     *   <value>rss/_doc</value>
     *  </list>
     * </property>
     * }
     * </pre>
     * @param mappings Array of indexname/_doc
     */
    public void setMappings(String[] mappings) {
        this.mappings = mappings;
    }

    /**
     * Define aliases you want to manage with this factory
     * <br>use : aliasname:indexname form
     * <p>Example :</p>
     * <pre>
     * {@code
     * <property name="aliases">
     *  <list>
     *   <value>alltheworld:twitter</value>
     *   <value>alltheworld:rss</value>
     *  </list>
     * </property>
     * }
     * </pre>
     * @param aliases alias array of aliasname:indexname
     */
    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    /**
     * Define templates you want to manage with this factory
     * <p>Example :</p>
     *
     * <pre>
     * {@code
     * <property name="templates">
     *  <list>
     *   <value>template_1</value>
     *   <value>template_2</value>
     *  </list>
     * </property>
     * }
     * </pre>
     *
     * @param templates list of template
     */
    public void setTemplates(String[] templates) {
        this.templates = templates;
    }

    /**
     * Classpath root for index and mapping files (default : /es)
     * <p>Example :</p>
     * <pre>
     * {@code
     * <property name="classpathRoot" value="/es" />
     * }
     * </pre>
     * That means that the factory will look in es folder to find index and mappings settings.
     * <br>So if you want to define a mapping for the _doc mapping in the twitter index, you
     * should put a _doc.json file under /es/twitter/ folder.
     * @param classpathRoot Classpath root for index and mapping files
     * @see #setMappings(String[])
     */
    public void setClasspathRoot(String classpathRoot) {
        // For compatibility reasons, we need to convert "/classpathroot" to "classpathroot"
        if (classpathRoot.startsWith("/")) {
            this.classpathRoot = classpathRoot.substring(1, classpathRoot.length());
        } else {

            this.classpathRoot = classpathRoot;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Starting Elasticsearch client");
        logger.warn("Elasticsearch Transport client is deprecated. You should switch to the Rest Client.");

        if (async) {
            Assert.notNull(taskExecutor, "taskExecutor can not be null");
            Future<Client> future = taskExecutor.submit(new Callable<Client>() {
                @Override
                public Client call() throws Exception {
                    return initialize();
                }
            });

            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setProxyTargetClass(true);
            proxyFactory.setTargetClass(Client.class);
            proxyFactory.addAdvice(new GenericInvocationHandler(future));
            proxyfiedClient = (Client) proxyFactory.getProxy();
        } else {
            client = initialize();
        }
    }

    private Client initialize() throws Exception {
        client = buildClient();
        if (autoscan) {
            mappings = ElasticsearchRestClientFactoryBean.computeMappings(mappings, classpathRoot);
            templates = ElasticsearchRestClientFactoryBean.computeTemplates(templates, classpathRoot);
        }

        // We extract indexes and mappings to manage from mappings definition
        if (mappings != null && mappings.length > 0) {
            ClusterHealthRequestBuilder healthRequestBuilder = client.admin().cluster().prepareHealth().setWaitForYellowStatus();
            ClusterStateRequestBuilder clusterStateRequestBuilder = client.admin().cluster().prepareState();
            Map<String, Collection<String>> indices = ElasticsearchRestClientFactoryBean.getIndexMappings(mappings);
            for (String index : indices.keySet()) {
                clusterStateRequestBuilder.setIndices(index);
            }
            ClusterStateResponse clusterStateResponse = clusterStateRequestBuilder.get();

            boolean checkIndicesStatus = false;
            for (String index : indices.keySet()) {
                if (clusterStateResponse.getState().getMetaData().indices().containsKey(index)) {
                    healthRequestBuilder.setIndices(index);
                    checkIndicesStatus = true;
                }
            }

            if (checkIndicesStatus) {
                logger.debug("we have to check some indices status as they already exist...");
                ClusterHealthResponse healths = healthRequestBuilder.get();
                if (healths.isTimedOut()) {
                    logger.warn("we got a timeout when checking indices status...");
                    if (healths.getIndices() != null) {
                        for (ClusterIndexHealth health : healths.getIndices().values()) {
                            if (health.getStatus() == ClusterHealthStatus.RED) {
                                logger.warn("index [{}] is in RED state", health.getIndex());
                            } else {
                                logger.debug("index [{}] is in [{}] state", health.getIndex(), health.getStatus().name());
                            }
                        }
                    }
                }
            }
        }

        initTemplates();
        initMappings();
        initAliases();

        return client;
    }

    @Override
    public void destroy() throws Exception {
        try {
            logger.info("Closing Elasticsearch client");
            if (client != null) {
                client.close();
            }
        } catch (final Exception e) {
            logger.error("Error closing Elasticsearch client: ", e);
        }
    }

    @Override
    public Client getObject() throws Exception {
        return async ? proxyfiedClient : client;
    }

    @Override
    public Class<Client> getObjectType() {
        return Client.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Init templates if needed.
     * <p>
     * Note that you can force to recreate template using
     * {@link #setForceTemplate(boolean)}
     */
    private void initTemplates() throws Exception {
        if (templates != null && templates.length > 0) {
            for (String template : templates) {
                Assert.hasText(template, "Can not read template in ["
                        + template
                        + "]. Check that templates is not empty.");
                createTemplate(client, classpathRoot, template, forceTemplate);
            }
        }
    }

    /**
     * Init mapping if needed.
     * <p>Note that you can force to reinit mapping using {@link #setForceMapping(boolean)}
     */
    private void initMappings() throws Exception {
        checkClient();
        // We extract indexes and mappings to manage from mappings definition
        if (mappings != null && mappings.length > 0) {
            Map<String, Collection<String>> indices = ElasticsearchRestClientFactoryBean.getIndexMappings(mappings);

            // Let's initialize indexes and mappings if needed
            for (String index : indices.keySet()) {
                createIndex(client, classpathRoot, index, forceMapping);
                if (mergeSettings) {
                    updateSettings(client, classpathRoot, index);
                }

                createMapping(client, classpathRoot, index, mergeMapping);
            }
        }
    }

    /**
     * Init aliases if needed.
     */
    private void initAliases() throws Exception {
        if (aliases != null && aliases.length > 0) {
            for (String aliasIndex : aliases) {
                Tuple<String, String> aliasIndexSplitted = ElasticsearchRestClientFactoryBean.computeAlias(aliasIndex);
                createAlias(client, aliasIndexSplitted.v2(), aliasIndexSplitted.v1());
            }
        }
    }

    /**
     * Check if client is still here !
     */
    private void checkClient() throws Exception {
        if (client == null) {
            throw new Exception("Elasticsearch client doesn't exist. Your factory is not properly initialized.");
        }
    }

	private String[] esNodes =  { "localhost:9300" };

	private String[] plugins = { };

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

	/**
	 * Define optional plugins.
	 * <br>use : fully.qualified.class.name.Plugin form
	 * <p>Example :</p>
 	 * <pre>
	 * {@code
	 * <property name="plugins">
     *  <set>
     *   <value>org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin</value>
     *  </set>
     * </property>
	 * }
	 * </pre>
	 * @param plugins An array of fully qualified plugin classes
	 */
	public void setPlugins(String[] plugins) {
		this.plugins = plugins;
	}

	private Client buildClient() throws Exception {
		Settings.Builder settingsBuilder = Settings.builder();

        if (null != this.properties) {
            properties.forEach((key, value) -> settingsBuilder.put((String) key, (String) value));
        }

        List<Class<? extends Plugin>> pluginClasses = new ArrayList<>(plugins.length);
		for (String plugin : plugins) {
			logger.debug("Adding plugin [{}]", plugin);
            pluginClasses.add((Class<? extends Plugin>) ClassUtils.resolveClassName(plugin, Thread.currentThread().getContextClassLoader()));
		}

        TransportClient client;

        // We need to check if we have a user security property
        String securedUser = properties != null ? properties.getProperty(XPACK_USER, null) : null;
        if (securedUser != null) {
            logger.debug("Building a Secured XPack Transport Client");
            client = new PreBuiltXPackTransportClient(settingsBuilder.build(), pluginClasses);
        } else {
            logger.debug("Building a Transport Client");
            client = new PreBuiltTransportClient(settingsBuilder.build(), pluginClasses);
        }

        for (String esNode : esNodes) {
            client.addTransportAddress(toAddress(esNode));
        }

		return client;
	}

	/**
	 * Helper to define an hostname and port with a String like hostname:port
	 * @param address Node address hostname:port (or hostname)
	 */
	private TransportAddress toAddress(String address) throws UnknownHostException {
		if (address == null) return null;

		String[] splitted = address.split(":");
		int port = 9300;
		if (splitted.length > 1) {
			port = Integer.parseInt(splitted[1]);
		}

		return new TransportAddress(InetAddress.getByName(splitted[0]), port);
	}

}
