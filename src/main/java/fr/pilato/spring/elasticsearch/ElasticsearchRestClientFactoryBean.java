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

import fr.pilato.elasticsearch.tools.util.ResourceList;
import fr.pilato.elasticsearch.tools.util.SettingsFinder;
import fr.pilato.spring.elasticsearch.util.Tuple;
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
import java.util.List;

import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchAliasUpdater.createAlias;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchIndexUpdater.createIndex;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchIndexUpdater.updateSettings;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchTemplateUpdater.createTemplate;
import static fr.pilato.elasticsearch.tools.util.ResourceList.findIndexNames;

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
 * <p>In the following example, we will create two indices :</p>
 * <ul>
 *   <li>twitter
 *   <li>rss
 * </ul>
 * Then we will define an alias alltheworld for twitter and rss indexes.
 *
 * <pre>
 * {@code
 *  <bean id="esClient"
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean" >
 *    <property name="indices">
 *      <list>
 *        <value>twitter</value>
 *        <value>rss</value>
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
 {
  "settings" : {
   "number_of_shards" : 3,
   "number_of_replicas" : 2
  },
  "mappings" : {
   "properties" : {
    "message" : {"type" : "text"}
   }
  }
 }
 * </pre>
 *
 * By convention, the factory will create all settings found under the /es classpath.<br>
 * You can disable convention and use configuration by setting autoscan to false.
 *
 * @see RestHighLevelClient
 * @author David Pilato
 */
public class ElasticsearchRestClientFactoryBean extends ElasticsearchAbstractFactoryBean
        implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchRestClientFactoryBean.class);

    private RestHighLevelClient client;

    private boolean forceIndex;

    private boolean forceTemplate;

    private boolean mergeSettings = true;

    private boolean autoscan = true;

    private String[] indices;

    private String[] aliases;

    private String[] templates;

    private String classpathRoot = "es";

    private String[] esNodes =  { "http://localhost:9200" };

    /**
     * Set to true if you want to force reinit the indices. This will remove all existing indices managed by the factory.
     * @param forceIndex true if you want to force reinit the indices
     */
    public void setForceIndex(boolean forceIndex) {
        this.forceIndex = forceIndex;
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
    @Deprecated
    public void setMergeMapping(boolean mergeMapping) {
        logger.warn("This setting has been removed as we only manage index settings from 7.0.");
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
     * <br>use : indexname form
     * <p>Example :</p>
     * <pre>
     * {@code
     * <property name="mappings">
     *  <list>
     *   <value>twitter</value>
     *   <value>rss</value>
     *  </list>
     * </property>
     * }
     * </pre>
     * @param indices Array of index names
     */
    public void setIndices(String[] indices) {
        this.indices = indices;
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
     * That means that the factory will look in es folder to find index settings.
     * <br>So if you want to define settings or mappings for the twitter index, you
     * should put a _settings.json file under /es/twitter/ folder.
     * @param classpathRoot Classpath root for index and mapping files
     * @see #setIndices(String[])
     */
    public void setClasspathRoot(String classpathRoot) {
        // For compatibility reasons, we need to convert "/classpathroot" to "classpathroot"
        if (classpathRoot.startsWith("/")) {
            this.classpathRoot = classpathRoot.substring(1);
        } else {

            this.classpathRoot = classpathRoot;
        }
    }

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

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Starting Elasticsearch client");
        client = initialize();
    }

    private RestHighLevelClient initialize() throws Exception {
        client = buildRestHighLevelClient();
        if (autoscan) {
            indices = computeIndexNames(indices, classpathRoot);
            templates = computeTemplates(templates, classpathRoot);
        }

        initTemplates();
        initSettings();
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
     * We use convention over configuration : see https://github.com/dadoonet/spring-elasticsearch/issues/3
     */
    static String[] computeIndexNames(String[] indices, String classpathRoot) {
        if (indices == null || indices.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Automatic discovery is activated. Looking for definition files in classpath under [{}].",
                        classpathRoot);
            }

            try {
                // Let's scan our resources
                return findIndexNames(classpathRoot).toArray(new String[0]);
            } catch (IOException |URISyntaxException e) {
                logger.debug("Automatic discovery does not succeed for finding json files in classpath under " + classpathRoot + ".");
                logger.trace("", e);
            }
        }
        return indices;
    }

    /**
     * We use convention over configuration : see https://github.com/dadoonet/spring-elasticsearch/issues/3
     */
    static String[] computeTemplates(String[] templates, String classpathRoot) {
        if (templates == null || templates.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Automatic discovery is activated. Looking for template files in classpath under [{}].",
                        classpathRoot);
            }

            ArrayList<String> autoTemplates = new ArrayList<>();

            try {
                // Let's scan our resources
                List<String> scannedTemplates = ResourceList.getResourceNames(classpathRoot, SettingsFinder.Defaults.TemplateDir);
                autoTemplates.addAll(scannedTemplates);

                return autoTemplates.toArray(new String[0]);
            } catch (IOException|URISyntaxException e) {
                logger.debug("Automatic discovery does not succeed for finding json files in classpath under " + classpathRoot + ".");
                logger.trace("", e);
            }
        }
        return templates;
    }

    /**
     * Init index settings if needed.
     * <p>Note that you can force to reinit the index using {@link #setForceIndex(boolean)}
     */
    private void initSettings() throws Exception {
        checkClient();
        // We extract indexes and mappings to manage from mappings definition
        if (indices != null && indices.length > 0) {
            // Let's initialize indexes and mappings if needed
            for (String index : indices) {
                createIndex(client.getLowLevelClient(), classpathRoot, index, forceIndex);
                if (mergeSettings) {
                    updateSettings(client.getLowLevelClient(), classpathRoot, index);
                }
            }
        }
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
     * Init aliases if needed.
     */
    private void initAliases() throws Exception {
		if (aliases != null && aliases.length > 0) {
			for (String aliasIndex : aliases) {
                Tuple<String, String> aliasIndexSplitted = computeAlias(aliasIndex);
				createAlias(client.getLowLevelClient(), aliasIndexSplitted.v2(), aliasIndexSplitted.v1());
			}
		}
    }

    static Tuple<String, String> computeAlias(String aliasIndex) {
        String[] aliasIndexSplitted = aliasIndex.split(":");
        String alias = aliasIndexSplitted[0];
        String index = aliasIndexSplitted[1];

        if (index == null) throw new IllegalArgumentException("Can not read index in [" + aliasIndex +
                "]. Check that aliases contains only aliasname:indexname elements.");
        if (alias == null) throw new IllegalArgumentException("Can not read mapping in [" + aliasIndex +
                "]. Check that aliases contains only aliasname:indexname elements.");

        return new Tuple<>(index, alias);
    }

    /**
     * Check if client is still here !
     */
    private void checkClient() throws Exception {
        if (client == null) {
            throw new Exception("Elasticsearch client doesn't exist. Your factory is not properly initialized.");
        }
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
