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
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * An abstract {@link FactoryBean} used to create an Elasticsearch
 * {@link RestHighLevelClient}.
 * <p>
 * The lifecycle of the underlying {@link RestHighLevelClient} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link RestClient#close()}
 * </p>
 * <p>
 * You need to define the nodes you want to communicate with.<br>
 * Don't forget to create an es.properties file if you want to set specific values
 * for this client, e.g.: cluster.name
 * <br>Example :
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
 * </p>
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
 * @see RestClient
 * @author David Pilato
 */
public class ElasticsearchHighLevelRestClientFactoryBean extends ElasticsearchAbstractFactoryBean
        implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHighLevelRestClientFactoryBean.class);

    private RestClient client;
    private RestHighLevelClient highLevelClient;
    private RestHighLevelClient proxyfiedClient;

    public void setClient(RestClient client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Starting Elasticsearch High Level client");

        if (async) {
            Assert.notNull(taskExecutor, "taskExecutor can not be null");
            Future<RestHighLevelClient> future = taskExecutor.submit(new Callable<RestHighLevelClient>() {
                @Override
                public RestHighLevelClient call() throws Exception {
                    return initialize();
                }
            });

            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setProxyTargetClass(true);
            proxyFactory.setTargetClass(RestHighLevelClient.class);
            proxyFactory.addAdvice(new GenericInvocationHandler(future));
            proxyfiedClient = (RestHighLevelClient) proxyFactory.getProxy();
        } else {
            highLevelClient = initialize();
        }
    }

    private RestHighLevelClient initialize() throws Exception {
        return new RestHighLevelClient(client);
    }

    @Override
    public RestHighLevelClient getObject() throws Exception {
        return async ? proxyfiedClient : highLevelClient;
    }

    @Override
    public Class<RestHighLevelClient> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
    }
}
