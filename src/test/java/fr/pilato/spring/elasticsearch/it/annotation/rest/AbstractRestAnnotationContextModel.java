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

package fr.pilato.spring.elasticsearch.it.annotation.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.spring.elasticsearch.it.annotation.AbstractAnnotationContextModel;
import fr.pilato.spring.elasticsearch.proxy.GenericInvocationHandler;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractRestAnnotationContextModel extends AbstractAnnotationContextModel {
    protected RestHighLevelClient checkClient(String name) {
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

    protected boolean isMappingExist(RestClient client, String index, String type) {
        try {
            return client.performRequest(new Request("HEAD", "/" + index + "/_mapping/" + type)).getStatusLine().getStatusCode() != 404;
        } catch (IOException e) {
            return false;
        }
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
            client.index(new IndexRequest("twitter").type("_doc").id("1").source("{\"foo\":\"bar\"}", XContentType.JSON), RequestOptions.DEFAULT);
            assertThat(client.get(new GetRequest("twitter").type("_doc").id("1"), RequestOptions.DEFAULT).isExists(), is(true));
        }
    }

    /**
     * Overwrite it to implement use case specific tests
     * @param client Client representing bean named esClient
     */
    protected void checkUseCaseSpecific(RestHighLevelClient client) throws Exception {
    }

    protected void assertShardsAndReplicas(RestClient client, String indexName, int expectedShards, int expectedReplicas) throws IOException {
        Map<String, Object> result = runRestQuery(client, "/" + indexName + "/_settings", indexName, "settings", "index");
        assertThat(Integer.parseInt((String) result.get("number_of_shards")), is(expectedShards));
        assertThat(Integer.parseInt((String) result.get("number_of_replicas")), is(expectedReplicas));
    }

    protected Map<String, Object> runRestQuery(RestClient client, String url, String... fields) throws IOException {
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
