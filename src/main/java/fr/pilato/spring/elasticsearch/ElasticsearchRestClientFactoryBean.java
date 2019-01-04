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

import fr.pilato.elasticsearch.tools.index.IndexFinder;
import fr.pilato.elasticsearch.tools.template.TemplateFinder;
import fr.pilato.elasticsearch.tools.type.TypeFinder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.pilato.elasticsearch.tools.alias.AliasElasticsearchUpdater.createAlias;
import static fr.pilato.elasticsearch.tools.index.IndexElasticsearchUpdater.createIndex;
import static fr.pilato.elasticsearch.tools.index.IndexElasticsearchUpdater.updateSettings;
import static fr.pilato.elasticsearch.tools.template.TemplateElasticsearchUpdater.createTemplate;
import static fr.pilato.elasticsearch.tools.type.TypeElasticsearchUpdater.createMapping;

/**
 * An abstract {@link org.springframework.beans.factory.FactoryBean} used to create an Elasticsearch
 * {@link org.elasticsearch.client.RestHighLevelClient}.
 * <p>
 * The lifecycle of the underlying {@link RestHighLevelClient} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link RestHighLevelClient#close()}
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
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean" >
 *    <property name="esNodes">
 *      <list>
 *        <value>localhost:9200</value>
 *        <value>localhost:9201</value>
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
 *   <li>tweet
 * </ul>
 * rss index will contain two types :
 * <ul>
 *   <li>feed
 *   <li>source
 * </ul>
 * Then we will define an alias alltheworld for twitter and rss indexes.
 *
 * <pre>
 * {@code
 *  <bean id="esClient"
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean" >
 *    <property name="mappings">
 *      <list>
 *        <value>twitter/tweet</value>
 *        <value>rss/feed</value>
 *        <value>rss/source</value>
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
 * But, if you define a file named /es/indexname/type.json in your classpath, the type will be created at startup using
 * the type definition you give.
 * <br>
 * So if you create a file named /es/twitter/tweet.json in your src/main/resources folder (for maven lovers),
 * it will be used by the factory to create the tweet type in twitter index.
 * <pre>
 * {
 *   "tweet" : {
 *     "properties" : {
 *       "message" : {"type" : "string", "store" : "yes"}
 *     }
 *   }
 * }
 * </pre>
 *
 * By convention, the factory will create all settings and mappings found under the /es classpath.<br>
 * You can disable convention and use configuration by setting autoscan to false.
 *
 * @see RestHighLevelClient
 * @author David Pilato
 */
public class ElasticsearchRestClientFactoryBean extends ElasticsearchAbstractFactoryBean
        implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchRestClientFactoryBean.class);

    private RestHighLevelClient client;

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
     * <br>use : indexname/mappingname form
     * <p>Example :</p>
     * <pre>
     * {@code
     * <property name="mappings">
     *  <list>
     *   <value>twitter/tweet</value>
     *   <value>rss/feed</value>
     *   <value>rss/source</value>
     *  </list>
     * </property>
     * }
     * </pre>
     * @param mappings Array of indexname/mappingname
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
     * <br>So if you want to define a mapping for the tweet mapping in the twitter index, you
     * should put a tweet.json file under /es/twitter/ folder.
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
        client = initialize();
    }

    private RestHighLevelClient initialize() throws Exception {
        client = buildRestHighLevelClient();
        if (autoscan) {
            computeMappings();
            computeTemplates();
        }

        initTemplates();
        initMappings();
        initAliases();

        return client;
    }

    @Override
    public void destroy() {
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
    public RestHighLevelClient getObject() {
        return client;
    }

    @Override
    public Class<RestHighLevelClient> getObjectType() {
        return RestHighLevelClient.class;
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
                createTemplate(client.getLowLevelClient(), classpathRoot, template, forceTemplate);
            }
        }
    }

    /**
     * We use convention over configuration : see https://github.com/dadoonet/spring-elasticsearch/issues/3
     */
    private void computeMappings() {
        if (mappings == null || mappings.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Automatic discovery is activated. Looking for definition files in classpath under [{}].",
                        classpathRoot);
            }

            ArrayList<String> autoMappings = new ArrayList<>();

            try {
                // Let's scan our resources
                Collection<String> indices = IndexFinder.findIndexNames(classpathRoot);
                for (String index : indices) {
                    Collection<String> types = TypeFinder.findTypes(classpathRoot, index);
                    if (types.isEmpty()) {
                        autoMappings.add(index);
                    } else {
                        for (String type : types) {
                            autoMappings.add(index+"/"+type);
                        }
                    }
                }

                mappings = autoMappings.toArray(new String[autoMappings.size()]);
            } catch (IOException |URISyntaxException e) {
                logger.debug("Automatic discovery does not succeed for finding json files in classpath under " + classpathRoot + ".");
                logger.trace("", e);
            }
        }
    }

    /**
     * We use convention over configuration : see https://github.com/dadoonet/spring-elasticsearch/issues/3
     */
    private void computeTemplates() {
        if (templates == null || templates.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Automatic discovery is activated. Looking for template files in classpath under [{}].",
                        classpathRoot);
            }

            ArrayList<String> autoTemplates = new ArrayList<>();

            try {
                // Let's scan our resources
                List<String> scannedTemplates = TemplateFinder.findTemplates(classpathRoot);
                for (String template : scannedTemplates) {
                    autoTemplates.add(template);
                }

                templates = autoTemplates.toArray(new String[autoTemplates.size()]);
            } catch (IOException|URISyntaxException e) {
                logger.debug("Automatic discovery does not succeed for finding json files in classpath under " + classpathRoot + ".");
                logger.trace("", e);
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
            Map<String, Collection<String>> indices = getIndexMappings(mappings);

            // Let's initialize indexes and mappings if needed
            for (String index : indices.keySet()) {
                createIndex(client.getLowLevelClient(), classpathRoot, index, forceMapping);
                if (mergeSettings) {
                    updateSettings(client.getLowLevelClient(), classpathRoot, index);
                }

                Collection<String> mappings = indices.get(index);
                for (String type : mappings) {
                    createMapping(client.getLowLevelClient(), classpathRoot, index, type, mergeMapping);
                }
            }
        }
    }

    private static Map<String, Collection<String>> getIndexMappings(String[] mappings) throws Exception {
        Map<String, Collection<String>> indices = new HashMap<>();

        for (String indexmapping : mappings) {
            String[] indexmappingsplitted = indexmapping.split("/");
            String index = indexmappingsplitted[0];

            if (index == null) throw new Exception("Can not read index in [" + indexmapping +
                    "]. Check that mappings contains only indexname/mappingname elements.");

            // We add the mapping in the collection of its index
            if (!indices.containsKey(index)) {
                indices.put(index, new ArrayList<String>());
            }

            if (indexmappingsplitted.length > 1) {
                indices.get(index).add(indexmappingsplitted[1]);
            }
        }
        return indices;
    }

    /**
     * Init aliases if needed.
     */
    private void initAliases() throws Exception {
		if (aliases != null && aliases.length > 0) {
			for (String aliase : aliases) {
				String[] aliasessplitted = aliase.split(":");
				String alias = aliasessplitted[0];
				String index = aliasessplitted[1];

				if (index == null) throw new Exception("Can not read index in [" + aliase +
						"]. Check that aliases contains only aliasname:indexname elements.");
				if (alias == null) throw new Exception("Can not read mapping in [" + aliase +
						"]. Check that aliases contains only aliasname:indexname elements.");

				createAlias(client.getLowLevelClient(), alias, index);
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

	private String[] esNodes =  { "http://localhost:9200" };

    /**
	 * Define ES nodes to communicate with.
	 * <br>use : protocol://hostname:port form
	 * <p>Example :</p>
	 * <pre>
	 * {@code
	 * <property name="esNodes">
	 *  <list>
	 *   <value>http://localhost:9200</value>
	 *   <value>http://localhost:9201</value>
	 *  </list>
	 * </property>
	 * }
	 * </pre>
	 * If not set, default to [ "http://localhost:9200" ].
	 * <br>If port is not set, default to 9200.
	 * @param esNodes An array of nodes hostname:port
	 */
	public void setEsNodes(String[] esNodes) {
		this.esNodes = esNodes;
	}

	private RestHighLevelClient buildRestHighLevelClient() throws Exception {
        Collection<HttpHost> hosts = new ArrayList<>(esNodes.length);
		for (String esNode : esNodes) {
            hosts.add(HttpHost.create(esNode));
        }

        RestClientBuilder rcb = RestClient.builder(hosts.toArray(new HttpHost[]{}));

        // We need to check if we have a user security property
        String securedUser = properties != null ? properties.getProperty(XPACK_USER, null) : null;
        if (securedUser != null) {
            // We split the username and the password
            String[] split = securedUser.split(":");
            if (split.length < 2) {
                throw new IllegalArgumentException(XPACK_USER + " must have the form username:password");
            }
            String username = split[0];
            String password = split[1];

            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));

            rcb.setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(credentialsProvider));
        }

        return new RestHighLevelClient(rcb);
	}
}
