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
import fr.pilato.spring.elasticsearch.proxy.GenericInvocationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static fr.pilato.elasticsearch.tools.alias.AliasElasticsearchUpdater.createAlias;
import static fr.pilato.elasticsearch.tools.index.IndexElasticsearchUpdater.createIndex;
import static fr.pilato.elasticsearch.tools.index.IndexElasticsearchUpdater.updateSettings;
import static fr.pilato.elasticsearch.tools.template.TemplateElasticsearchUpdater.createTemplate;
import static fr.pilato.elasticsearch.tools.type.TypeElasticsearchUpdater.createMapping;

/**
 * An abstract {@link FactoryBean} used to create an ElasticSearch
 * {@link Client}.
 * <p>
 * The lifecycle of the underlying {@link Client} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link Client#close()}
 * </p>
 * <p>
 * You can define properties for this bean that will help to auto create indexes
 * and types.
 * <br>
 * By default, the factory will load a es.properties file in the classloader. It will
 * contains all information needed for your client, e.g.: cluster.name
 * <br>
 * If you want to  modify the filename used for properties, just define the settingsFile property.
 * <p>
 * In the following example, we will create two indexes :
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
 * </p>
 * 
 * <pre>
 * {@code
 *  <bean id="esClient"
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
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
 * {@code
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
 * {@code
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
 * @see {@link ElasticsearchTransportClientFactoryBean} to get a *simple*
 *      client.
 * @see {@link ElasticsearchClientFactoryBean} to get a client from a cluster
 *      node.
 * @author David Pilato
 */
public abstract class ElasticsearchAbstractClientFactoryBean extends ElasticsearchAbstractFactoryBean 
	implements FactoryBean<Client>,	InitializingBean, DisposableBean {

	private static final Logger logger = LogManager.getLogger(ElasticsearchAbstractClientFactoryBean.class);

	protected Client client;
	protected Client proxyfiedClient;

	protected boolean forceMapping;
	
	protected boolean forceTemplate;
	
	protected boolean mergeMapping;
	
	protected boolean mergeSettings = true;
	
	protected boolean autoscan = true;
	
	protected String[] mappings;

	protected String[] aliases;
	
	protected String[] templates;
	
	protected String classpathRoot = "es";
	
	/**
	 * Implement this method to build a client
	 * @return ES Client
	 * @throws Exception if something goes wrong
	 */
	abstract protected Client buildClient() throws Exception;
	
	/**
	 * Set to true if you want to force reinit indexes/mapping
	 * @param forceMapping
	 */
	public void setForceMapping(boolean forceMapping) {
		this.forceMapping = forceMapping;
	}

	/**
	 * Set to true if you want to force recreate templates
	 * 
	 * @param forceTemplate
	 */
	public void setForceTemplate(boolean forceTemplate) {
		this.forceTemplate = forceTemplate;
	}
	
	/**
	 * Set to true if you want to try to merge mappings
	 * @param mergeMapping
	 */
	public void setMergeMapping(boolean mergeMapping) {
		this.mergeMapping = mergeMapping;
	}

	/**
	 * Set to true if you want to try to merge index settings
	 * @param mergeSettings
	 */
	public void setMergeSettings(boolean mergeSettings) {
		this.mergeSettings = mergeSettings;
	}
	
	/**
	 * Set to false if you want to use configuration instead of convention.
	 * @param autoscan
	 */
	public void setAutoscan(boolean autoscan) {
		this.autoscan = autoscan;
	}
	
	
	/**
	 * Define mappings you want to manage with this factory
	 * <br/>use : indexname/mappingname form
	 * <p>Example :<br/>
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
	 * <br/>use : aliasname:indexname form
	 * <p>Example :<br/>
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
	 * Define templates you want to manage with this factory <br/>
	 * <p>
	 * Example :<br/>
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
	 * <p>Example :<br/>
 	 * <pre>
	 * {@code
	 * <property name="classpathRoot" value="/es" />
	 * }
	 * </pre>
	 * That means that the factory will look in es folder to find index and mappings settings.
	 * <br/>So if you want to define a mapping for the tweet mapping in the twitter index, you
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
		logger.info("Starting ElasticSearch client");
		
		if (async) {
			Assert.notNull(taskExecutor);
			Future<Client> future = taskExecutor.submit(new Callable<Client>() {
				@Override
				public Client call() throws Exception {
					return initialize();
				}
			});
			proxyfiedClient = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(),
					new Class[]{Client.class}, new GenericInvocationHandler(future));
		} else {
			client = initialize();
		}
	}

	private Client initialize() throws Exception {
		client = buildClient();
		// TODO Only wait for potential indices
		client.admin().cluster().prepareHealth().setWaitForYellowStatus().get();
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
	public void destroy() throws Exception {
		try {
			logger.info("Closing ElasticSearch client");
			if (client != null) {
				client.close();
			}
		} catch (final Exception e) {
			logger.error("Error closing ElasticSearch client: ", e);
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
	 * 
	 * @throws Exception
	 */
	private void initTemplates() throws Exception {
		if (templates != null && templates.length > 0) {
			for (int i = 0; i < templates.length; i++) {
				String template = templates[i];
				Assert.hasText(template, "Can not read template in ["
						+ templates[i]
						+ "]. Check that templates is not empty.");
				createTemplate(client, classpathRoot, template, forceTemplate);
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
			} catch (IOException e) {
				logger.debug("Automatic discovery does not succeed for finding json files in classpath under " + classpathRoot + ".");
				logger.trace(e);
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
			} catch (IOException e) {
				logger.debug("Automatic discovery does not succeed for finding json files in classpath under " + classpathRoot + ".");
				logger.trace(e);
			}
		}
	}

	/**
	 * Init mapping if needed.
	 * <p>Note that you can force to reinit mapping using {@link #setForceMapping(boolean)}
	 * @throws Exception 
	 */
	private void initMappings() throws Exception {
		checkClient();
		// We extract indexes and mappings to manage from mappings definition
		if (mappings != null && mappings.length > 0) {
			Map<String, Collection<String>> indexes = new HashMap<>();
			
			for (int i = 0; i < mappings.length; i++) {
				String indexmapping = mappings[i];
				String[] indexmappingsplitted = indexmapping.split("/");
				String index = indexmappingsplitted[0];

                if (index == null) throw new Exception("Can not read index in [" + indexmapping +
                        "]. Check that mappings contains only indexname/mappingname elements.");

                // We add the mapping in the collection of its index
                if (!indexes.containsKey(index)) {
                    indexes.put(index, new ArrayList<String>());
                }

                if (indexmappingsplitted.length > 1) {
                    String mapping = indexmappingsplitted[1];
                    indexes.get(index).add(mapping);
                }
			}
			
			// Let's initialize indexes and mappings if needed
			for (String index : indexes.keySet()) {
				createIndex(client, classpathRoot, index);
				if (mergeSettings) {
					updateSettings(client, classpathRoot, index);
				}

				Collection<String> mappings = indexes.get(index);
				for (Iterator<String> iterator = mappings.iterator(); iterator
						.hasNext();) {
					String type = iterator.next();
					createMapping(client, classpathRoot, index, type, mergeMapping, forceMapping);
				}
			}
		}
	}

	/**
	 * Init aliases if needed.
	 * @throws Exception 
	 */
	private void initAliases() throws Exception {
		if (aliases != null && aliases.length > 0) {
			for (int i = 0; i < aliases.length; i++) {
				String[] aliasessplitted = aliases[i].split(":");
				String alias = aliasessplitted[0];
				String index = aliasessplitted[1];
				
				if (index == null) throw new Exception("Can not read index in [" + aliases[i] + 
						"]. Check that aliases contains only aliasname:indexname elements.");
				if (alias == null) throw new Exception("Can not read mapping in [" + aliases[i] + 
						"]. Check that aliases contains only aliasname:indexname elements.");

				createAlias(client, alias, index);
			}
		}
	}

	/**
	 * Check if client is still here !
	 * @throws Exception
	 */
	private void checkClient() throws Exception {
		if (client == null) {
			throw new Exception("ElasticSearch client doesn't exist. Your factory is not properly initialized.");
		}
	}
}
