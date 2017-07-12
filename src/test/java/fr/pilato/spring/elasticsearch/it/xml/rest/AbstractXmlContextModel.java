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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractXmlContextModel extends BaseTest {
    private ConfigurableApplicationContext ctx;

    /**
     * @return list of xml files needed to be loaded for this test
     */
    abstract String[] xmlBeans();

    @Before
    public void startContext() {
        String[] xmlBeans = xmlBeans();
        if (xmlBeans.length == 0) {
            fail("Can not start a factory without any context!");
        }
        logger.info("  --> Starting Spring Context with [{}] file(s)", xmlBeans.length);
        for (String xmlBean : xmlBeans) {
            logger.info("    - {}", xmlBean);
        }
        ctx = new ClassPathXmlApplicationContext(xmlBeans);
    }

    @After
    public void stopContext() {
        if (ctx != null) {
            logger.info("  --> Closing Spring Context");
            ctx.close();
        }
    }

    RestClient checkClient(String name) {
        return checkClient(name, null);
    }

    RestClient checkClient(String name, Boolean async) {
        RestClient client;

        if (name != null) {
            client = ctx.getBean(name, RestClient.class);
        } else {
            client = ctx.getBean(RestClient.class);
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

    boolean isMappingExist(RestClient client, String index, String type) {
        try {
            return client.performRequest("HEAD", "/" + index + "/_mapping/" + type).getStatusLine().getStatusCode() != 404;
        } catch (IOException e) {
            return false;
        }
    }

    @Test
    public void testFactoriesCreated() throws Exception {
        RestClient client = checkClient(beanName());
        checkUseCaseSpecific(client);

        // If an index is expected, let's check it actually exists
        if (indexName() != null) {
            // We test how many shards and replica we have
            assertShardsAndReplicas(client, indexName(), expectedShards(), expectedReplicas());

            // #92: search errors with async created Client
            Map<String, String> params = new HashMap<>();
            params.put("refresh", "true");


            client.performRequest("PUT", "/twitter/tweet/1", params, new StringEntity("{\"foo\":\"bar\"}", ContentType.APPLICATION_JSON));
            try {
                client.performRequest("GET", "/twitter/tweet/1");
            } catch (ResponseException e) {
                assertThat(e.getResponse().getStatusLine().getStatusCode(), not(404));
                throw e;
            }
        }
    }

    /**
     * Overwrite it to implement use case specific tests
     * @param client Client representing bean named esClient
     */
    protected void checkUseCaseSpecific(RestClient client) throws Exception {
    }

    /**
     * Overwrite it if the number of expected shards is not 5
     * @return Number of expected primaries
     */
    protected int expectedShards() {
        return 5;
    }

    /**
     * Overwrite it if the number of expected replicas is not 1
     * @return Number of expected replicas
     */
    protected int expectedReplicas() {
        return 1;
    }


    protected String beanName() {
        return "esClient";
    }

    void assertShardsAndReplicas(RestClient client, String indexName, int expectedShards, int expectedReplicas) throws IOException {
        Map<String, Object> result = runRestQuery(client, "/" + indexName + "/_settings", indexName, "settings", "index");
        assertThat(Integer.parseInt((String) result.get("number_of_shards")), is(expectedShards));
        assertThat(Integer.parseInt((String) result.get("number_of_replicas")), is(expectedReplicas));
    }

    Map<String, Object> runRestQuery(RestClient client, String url, String... fields) throws IOException {
        Response response = client.performRequest("GET", url);
        Map<String, Object> result = new ObjectMapper().readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});
        logger.trace("Raw result {}", result);
        if (fields.length > 0) {
            result = extractFromPath(result, fields);
            logger.trace("Extracted result {}", result);
        }
        return result;
    }

    protected static Map<String, Object> extractFromPath(Map<String, Object> json, String... path) {
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
