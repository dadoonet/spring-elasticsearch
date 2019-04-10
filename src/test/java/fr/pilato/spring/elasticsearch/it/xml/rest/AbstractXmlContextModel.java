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

package fr.pilato.spring.elasticsearch.it.xml.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.spring.elasticsearch.it.BaseTest;
import fr.pilato.spring.elasticsearch.proxy.GenericInvocationHandler;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractXmlContextModel extends BaseTest {
    private ConfigurableApplicationContext ctx;

    /**
     * @return list of xml files needed to be loaded for this test
     */
    abstract String[] xmlBeans();

    @BeforeEach
    void startContext() {
        String[] xmlBeans = xmlBeans();

        // Let's hack the context depending if the test cluster is running securely or not
        if (securityInstalled) {
            String[] securedXmlBeans = new String[xmlBeans.length];
            for (int i = 0; i < xmlBeans.length; i++) {
                securedXmlBeans[i] = xmlBeans[i].replace("models/rest/", "models/rest-xpack/");
            }
            xmlBeans = securedXmlBeans;
        }

        if (xmlBeans.length == 0) {
            fail("Can not start a factory without any context!");
        }
        logger.info("  --> Starting Spring Context with [{}] file(s)", xmlBeans.length);
        for (String xmlBean : xmlBeans) {
            logger.info("    - {}", xmlBean);
        }
        ctx = new ClassPathXmlApplicationContext(xmlBeans);
    }

    @AfterEach
    void stopContext() {
        if (ctx != null) {
            logger.info("  --> Closing Spring Context");
            ctx.close();
        }
    }

    RestHighLevelClient checkClient(String name) {
        return checkClient(name, null);
    }

    RestHighLevelClient checkClient(String name, Boolean async) {
        RestHighLevelClient client;

        if (name != null) {
            client = ctx.getBean(name, RestHighLevelClient.class);
        } else {
            client = ctx.getBean(RestHighLevelClient.class);
        }
        if (async != null) {
            if (async) {
                assertThat(client, instanceOf(Advised.class));
                assertThat(((Advised) client).getAdvisors()[0].getAdvice() , instanceOf(GenericInvocationHandler.class));
            } else {
                try {
                    Proxy.getInvocationHandler(client);
                    fail("Must not be proxyfied");
                } catch (IllegalArgumentException e) {
                    // We expect that
                }
            }
        }
        assertThat(client, not(nullValue()));
        return client;
    }

    @Test
    public void testFactoriesCreated() throws Exception {
        RestHighLevelClient client = checkClient(beanName());
        checkUseCaseSpecific(client);

        // If an index is expected, let's check it actually exists
        if (indexName() != null) {
            // We test how many shards and replica we have
            assertShardsAndReplicas(client.getLowLevelClient(), indexName(), expectedShards(), expectedReplicas());

            // #92: search errors with async created Client
            client.index(new IndexRequest("twitter").id("1").source("{\"foo\":\"bar\"}", XContentType.JSON), RequestOptions.DEFAULT);
            assertThat(client.get(new GetRequest("twitter").id("1"), RequestOptions.DEFAULT).isExists(), is(true));
        }
    }

    /**
     * Overwrite it to implement use case specific tests
     * @param client Client representing bean named esClient
     */
    protected void checkUseCaseSpecific(RestHighLevelClient client) throws Exception {
    }

    /**
     * Overwrite it if the number of expected shards is not 1
     * @return Number of expected primaries
     */
    protected int expectedShards() {
        return 1;
    }

    /**
     * Overwrite it if the number of expected replicas is not 1
     * @return Number of expected replicas
     */
    protected int expectedReplicas() {
        return 1;
    }


    private String beanName() {
        return "esClient";
    }

    void assertShardsAndReplicas(RestClient client, String indexName, int expectedShards, int expectedReplicas) throws IOException {
        Map<String, Object> result = runRestQuery(client, "/" + indexName + "/_settings", indexName, "settings", "index");
        assertThat(Integer.parseInt((String) result.get("number_of_shards")), is(expectedShards));
        assertThat(Integer.parseInt((String) result.get("number_of_replicas")), is(expectedReplicas));
    }

    Map<String, Object> runRestQuery(RestClient client, String url, String... fields) throws IOException {
        Response response = client.performRequest(new Request("GET", url));
        Map<String, Object> result = new ObjectMapper().readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});
        logger.trace("Raw result {}", result);
        if (fields.length > 0) {
            result = extractFromPath(result, fields);
            logger.trace("Extracted result {}", result);
        }
        return result;
    }

    private static Map<String, Object> extractFromPath(Map<String, Object> json, String... path) {
        Map<String, Object> currentObject = json;
        for (String fieldName : path) {
            Object jObject = currentObject.get(fieldName);
            if (jObject == null) {
                throw new RuntimeException("incorrect Json. Was expecting field " + fieldName);
            }
            if (!(jObject instanceof Map)) {
                throw new RuntimeException("incorrect datatype in json. Expected Map and got " + jObject.getClass().getName());
            }
            currentObject = (Map<String, Object>) jObject;
        }
        return currentObject;
    }
}
