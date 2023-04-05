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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import fr.pilato.elasticsearch.tools.util.ResourceList;
import fr.pilato.elasticsearch.tools.util.SettingsFinder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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
import java.util.Properties;

import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchAliasUpdater.manageAliases;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchAliasUpdater.manageAliasesWithJsonInElasticsearch;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchComponentTemplateUpdater.createComponentTemplate;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchIndexLifecycleUpdater.createIndexLifecycle;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchIndexTemplateUpdater.createIndexTemplate;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchIndexUpdater.createIndex;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchIndexUpdater.updateSettings;
import static fr.pilato.elasticsearch.tools.updaters.ElasticsearchPipelineUpdater.createPipeline;
import static fr.pilato.elasticsearch.tools.util.ResourceList.findIndexNames;

/**
 * An abstract {@link FactoryBean} used to create an Elasticsearch
 * {@link ElasticsearchClient}.
 * <p>
 * The lifecycle of the underlying {@link ElasticsearchClient} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link RestClient#close()}
 * </p>
 * <p>
 * If not using the default https://localhost:9200, you need to define the nodes you want to communicate with
 * and probably the credentials.
 * </p>
 * <p>Example :</p>
 * <pre>
 * {@code
 * @Configuration
 * public class AppConfig {
 *    @Bean
 *    public ElasticsearchClient esClient() throws Exception {
 * 		ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
 * 	    factory.setEsNodes(List.of(HttpHost.create("https://localhost:9200")));
 * 		factory.setPassword("changeme");
 * 		factory.afterPropertiesSet();
 * 		return factory.getObject();
 *    }
 *  }
 * }
 * </pre>
 * <p>
 * The factory is meant to be used with some classpath files which are automatically
 * loaded from {@code /es} directory to define your:
 * <ul>
 *   <li>component templates: {@code /es/_component_templates/*.json}
 *   <li>index templates: {@code /es/_index_templates/*.json}
 *   <li>indices: {@code /es/INDEXNAME/_settings.json}
 *   <li>aliases: {@code /es/_aliases.json}
 *   <li>index lifecycles policies: {@code /es/_index_lifecycles/*.json}
 *   <li>ingest pipelines: {@code /es/_pipelines/*.json}
 * </ul>
 *
 * <p>You can force the manual creation of the above components but this is
 * not the goal of this factory. So it should be used only for some specific use cases:
 * </p>
 * <p>
 * Example :
 * </p>
 * <pre>
 * {@code
 * @Configuration
 * public class AppConfig {
 *    @Bean
 *    public ElasticsearchClient esClient() throws Exception {
 *      ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
 * 	    // Create two indices twitter and rss
 * 	    factory.setIndices({ "twitter", "rss" });
 * 	    // Create an alias alltheworld on top of twitter and rss indices
 * 	    factory.setAliases({ "alltheworld:twitter", "alltheworld:rss" });
 * 	    // Remove all the existing declared indices (twitter and rss). VERY DANGEROUS SETTING!
 * 	    factory.setForceIndex(true);
 * 	    // If setForceIndex is not set, we try to merge existing index settings
 * 	    // with the provided ones
 * 	    factory.setMergeSettings(true);
 * 	    // Disable automatic scanning of the classpath.
 * 	    factory.setAutoscan(false);
 *      factory.afterPropertiesSet();
 *      return factory.getObject();
 *    }
 *  }
 * }
 * </pre>
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
 * @see ElasticsearchClient
 * @author David Pilato
 */
public class ElasticsearchClientFactoryBean
        implements FactoryBean<ElasticsearchClient>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchClientFactoryBean.class);

    /**
     * Define the username:password to use.
     * @deprecated Now deprecated with username and password settings.
     * @see ElasticsearchClientFactoryBean#setUsername(String)
     * @see ElasticsearchClientFactoryBean#setPassword(String)
     */
    @Deprecated
    public final static String XPACK_USER = "xpack.security.user";

    private RestClient lowLevelClient;

    private boolean forceIndex;

    private boolean mergeSettings = true;

    private boolean autoscan = true;

    /**
     * For tests purpose only. We allow no to check Self-Signed Certificates.
     */
    private boolean checkSelfSignedCertificates = true;

    // TODO add support for certificates

    private String username;
    private String password;

    // TODO add support for keys

    private String[] indices;

    private String[] aliases;

    private String[] componentTemplates;

    private String[] indexTemplates;

    private String[] pipelines;

    private String[] lifecycles;

    private String classpathRoot = "es";

    private Collection<HttpHost> esNodes = List.of(HttpHost.create("https://localhost:9200"));

    private ElasticsearchClient client;

    public RestClient getLowLevelClient() {
        return lowLevelClient;
    }

    /**
     * Elasticsearch properties
     * @param properties the properties
     * @deprecated it was only used to set xpack.security.user. Please use now {@link #setUsername(String)}
     * and {@link #setPassword(String)}.
     */
    @Deprecated
    public void setProperties(Properties properties) {
        String securedUser = properties.getProperty(XPACK_USER, null);
        if (securedUser != null) {
            logger.warn("Usage of xpack.security.user property has been deprecated. " +
                    "You should now use username and password factory settings.");

            // We split the username and the password
            String[] split = securedUser.split(":");
            if (split.length < 2) {
                throw new IllegalArgumentException(XPACK_USER + " must have the form username:password");
            }
            username = split[0];
            password = split[1];
        }
    }

    /**
     * Set to true if you want to force reinit the indices. This will remove all existing indices managed by the factory.
     * @param forceIndex true if you want to force reinit the indices
     */
    public void setForceIndex(boolean forceIndex) {
        this.forceIndex = forceIndex;
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
     * For tests purpose only. We allow no to check Self-Signed Certificates.
     * @param checkSelfSignedCertificates set it to false if you don't want to check the certificate.
     */
    public void setCheckSelfSignedCertificates(boolean checkSelfSignedCertificates) {
        this.checkSelfSignedCertificates = checkSelfSignedCertificates;
        if (!checkSelfSignedCertificates) {
            logger.warn("You disabled checking the https certificate. This could lead to " +
                    "'Man in the middle' attacks. This setting is only intended for tests.");
        }
    }

    /**
     * Define the Elasticsearch username. Defaults to "elastic".
     * @param username Elasticsearch username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Define the Elasticsearch password. Must be provided.
     * @param password Elasticsearch password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Define indices you want to manage with this factory
     * in case you are not using automatic discovery (see {@link #setAutoscan(boolean)})
     * @param indices Array of index names
     */
    public void setIndices(String[] indices) {
        this.indices = indices;
    }

    /**
     * Define aliases you want to manage with this factory
     * in case you are not using automatic discovery (see {@link #setAutoscan(boolean)})
     * @param aliases alias array of aliasname:indexname
     */
    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    /**
     * Define the index templates you want to manage with this factory
     * in case you are not using automatic discovery (see {@link #setAutoscan(boolean)})
     * @param indexTemplates list of index templates
     */
    public void setIndexTemplates(String[] indexTemplates) {
        this.indexTemplates = indexTemplates;
    }

    /**
     * Define component templates you want to manage with this factory
     * in case you are not using automatic discovery (see {@link #setAutoscan(boolean)})
     * @param componentTemplates list of component templates
     */
    public void setComponentTemplates(String[] componentTemplates) {
        this.componentTemplates = componentTemplates;
    }

    /**
     * Define the pipelines you want to manage with this factory
     * in case you are not using automatic discovery (see {@link #setAutoscan(boolean)})
     * @param pipelines list of pipelines
     */
    public void setPipelines(String[] pipelines) {
        this.pipelines = pipelines;
    }

    /**
     * Classpath root for index and mapping files (default : /es)
     * <p>Example :</p>
     * <pre>
     * {@code
     * factory.setClasspathRoot("/es");
     * }
     * </pre>
     * That means that the factory will look in es folder to find index settings.
     * <br>So if you want to define settings or mappings for the twitter index, you
     * should put a _settings.json file under /es/twitter/ folder.
     * @param classpathRoot Classpath root for index and mapping files
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
     * Defaults to [ "https://localhost:9200" ].
     * @param esNodes A collection of nodes
     */
    public void setEsNodes(Collection<HttpHost> esNodes) {
        this.esNodes = esNodes;
    }

    /**
     * Define ES nodes to communicate with.
     * <br>use : protocol://hostname:port form
     * @param esNodes An array of nodes hostname:port
     * @deprecated #setEsNodes(HttpHost[])
     */
    @Deprecated
    public void setEsNodes(String[] esNodes) {
        Collection<HttpHost> hosts = new ArrayList<>(esNodes.length);
        for (String esNode : esNodes) {
            hosts.add(HttpHost.create(esNode));
        }

        this.esNodes = hosts;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Starting Elasticsearch Low Level client");
        lowLevelClient = buildElasticsearchLowLevelClient();
        if (autoscan) {
            indices = computeIndexNames(indices, classpathRoot);
            componentTemplates = discoverFromClasspath(componentTemplates, classpathRoot, SettingsFinder.Defaults.ComponentTemplatesDir);
            indexTemplates = discoverFromClasspath(indexTemplates, classpathRoot, SettingsFinder.Defaults.IndexTemplatesDir);
            pipelines = discoverFromClasspath(pipelines, classpathRoot, SettingsFinder.Defaults.PipelinesDir);
            lifecycles = discoverFromClasspath(lifecycles, classpathRoot, SettingsFinder.Defaults.IndexLifecyclesDir);
        }

        initLifecycles();
        initPipelines();
        initTemplates();
        initSettings();
        initAliases();

        logger.info("Starting Elasticsearch client");

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(lowLevelClient, new JacksonJsonpMapper());

        // And create the API client
        client = new ElasticsearchClient(transport);
    }

    @Override
    public void destroy() {
        try {
            logger.info("Closing Elasticsearch Low Level client");
            if (lowLevelClient != null) {
                lowLevelClient.close();
            }
        } catch (final Exception e) {
            logger.error("Error closing Elasticsearch Low Level client: ", e);
        }
    }

    @Override
    public ElasticsearchClient getObject() {
        return client;
    }

    @Override
    public Class<ElasticsearchClient> getObjectType() {
        return ElasticsearchClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * We use convention over configuration : see <a href="https://github.com/dadoonet/spring-elasticsearch/issues/3">...</a>
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

    static String[] discoverFromClasspath(String[] resources, String classpathRoot, String subdir) {
        if (resources != null && resources.length > 0) {
            logger.debug("Resources are manually provided so we won't do any automatic discovery.");
            return resources;
        }

        logger.debug("Automatic discovery is activated. Looking for resource files in classpath under [{}/{}].",
                classpathRoot, subdir);

        ArrayList<String> autoResources = new ArrayList<>();

        try {
            // Let's scan our resources
            List<String> scannedResources = ResourceList.getResourceNames(classpathRoot, subdir);
            autoResources.addAll(scannedResources);

            return autoResources.toArray(new String[0]);
        } catch (IOException|URISyntaxException e) {
            logger.debug("Automatic discovery does not succeed for finding json files in classpath under [{}/{}].", classpathRoot, subdir);
            logger.trace("", e);
            return resources;
        }
    }

    /**
     * Init index settings if needed.
     * <p>Note that you can force to reinit the index using {@link #setForceIndex(boolean)}
     */
    private void initSettings() throws Exception {
        checkClient();
        // We extract indexes and mappings to manage from mappings definition
        if (indices != null) {
            // Let's initialize indexes and mappings if needed
            for (String index : indices) {
                createIndex(lowLevelClient, classpathRoot, index, forceIndex);
                if (mergeSettings) {
                    updateSettings(lowLevelClient, classpathRoot, index);
                }
            }
        }
    }

    /**
     * It creates or updates:
     * <ul>
     *     <li>component templates</li>
     *     <li>index templates</li>
     *     <li>legacy templates (deprecated)</li>
     * </ul>
     */
    private void initTemplates() throws Exception {
        if (componentTemplates != null) {
            for (String componentTemplate : componentTemplates) {
                Assert.hasText(componentTemplate, "Can not read component template in ["
                        + componentTemplate
                        + "]. Check that component template is not empty.");
                createComponentTemplate(lowLevelClient, classpathRoot, componentTemplate);
            }
        }
        if (indexTemplates != null) {
            for (String indexTemplate : indexTemplates) {
                Assert.hasText(indexTemplate, "Can not read component template in ["
                        + indexTemplate
                        + "]. Check that component template is not empty.");
                createIndexTemplate(lowLevelClient, classpathRoot, indexTemplate);
            }
        }
    }

    /**
     * It creates or updates the index pipelines
     */
    private void initPipelines() throws Exception {
        if (pipelines != null) {
            for (String pipeline : pipelines) {
                Assert.hasText(pipeline, "Can not read pipeline in ["
                        + pipeline
                        + "]. Check that pipeline is not empty.");
                createPipeline(lowLevelClient, classpathRoot, pipeline);
            }
        }
    }

    /**
     * It creates or updates the index lifecycles
     */
    private void initLifecycles() throws Exception {
        if (lifecycles != null) {
            for (String lifecycle : lifecycles) {
                Assert.hasText(lifecycle, "Can not read lifecycle in ["
                        + lifecycle
                        + "]. Check that lifecycle is not empty.");
                createIndexLifecycle(lowLevelClient, classpathRoot, lifecycle);
            }
        }
    }

    /**
     * Init aliases if needed.
     */
    private void initAliases() throws Exception {
		if (aliases != null && aliases.length > 0) {
            String request = "{\"actions\":[";
            boolean first = true;
			for (String aliasIndex : aliases) {
			    if (!first) {
                    request += ",";
                }
			    first = false;
                Tuple<String, String> aliasIndexSplitted = computeAlias(aliasIndex);
                request += "{\"add\":{\"index\":\"" + aliasIndexSplitted.v1() +"\",\"alias\":\"" + aliasIndexSplitted.v2() +"\"}}";
			}
            request += "]}";
            manageAliasesWithJsonInElasticsearch(lowLevelClient, request);
		}
		if (autoscan) {
		    manageAliases(lowLevelClient, classpathRoot);
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
        if (lowLevelClient == null) {
            throw new Exception("Elasticsearch Low Level client doesn't exist. Your factory is not properly initialized.");
        }
    }

	private RestClient buildElasticsearchLowLevelClient() {
        RestClientBuilder rcb = RestClient.builder(esNodes.toArray(new HttpHost[]{}));

        // We need to check if we have a user security property
        if (username == null || password == null) {
            throw new IllegalArgumentException("From version 8, you MUST define a user and a password to access Elasticsearch.");
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        rcb.setHttpClientConfigCallback(hcb -> {
            hcb.setDefaultCredentialsProvider(credentialsProvider);
            if (!checkSelfSignedCertificates) {
                // ONLY FOR TESTS
                hcb.setSSLContext(SSLUtils.yesSSLContext());
            }
            return hcb;
        });

        return rcb.build();
	}
}
