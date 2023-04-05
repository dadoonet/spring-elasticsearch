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

package fr.pilato.spring.elasticsearch.it;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.spring.elasticsearch.SSLUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

public abstract class BaseTest {
    private final static Logger staticLogger = LoggerFactory.getLogger(BaseTest.class);
    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private static RestClient client;

    public final static String DEFAULT_TEST_CLUSTER = "https://127.0.0.1:9200";
    private final static String DEFAULT_TEST_USER = "elastic";
    private final static String DEFAULT_TEST_PASSWORD = "changeme";

    public final static String testCluster = System.getProperty("tests.cluster", DEFAULT_TEST_CLUSTER);
    public final static String testClusterUser = System.getProperty("tests.cluster.user", DEFAULT_TEST_USER);
    public final static String testClusterPass = System.getProperty("tests.cluster.pass", DEFAULT_TEST_PASSWORD);

    private static void testClusterRunning() throws IOException {
        try {
            Response response = client.performRequest(new Request("GET", "/"));
            Map<String, Object> result = new ObjectMapper().readValue(response.getEntity().getContent(), new TypeReference<>() {
            });

            Map<String, Object> asMap = (Map<String, Object>) result.get("version");

            staticLogger.info("Starting integration tests against an external cluster running elasticsearch [{}]",
                    asMap.get("number"));
        } catch (ConnectException e) {
            // If we have an exception here, let's ignore the test
            staticLogger.warn("Integration tests are skipped: [{}]", e.getMessage());
            assumeFalse(e.getMessage().contains("Connection refused"), "Integration tests are skipped");
        } catch (IOException e) {
            staticLogger.error("Full error is", e);
            throw e;
        }
    }


    @BeforeAll
    public static void startElasticsearchClient() {
        try {
            if (client == null) {
                staticLogger.info("Starting tests against {}", testCluster);
                RestClientBuilder builder = RestClient.builder(HttpHost.create(testCluster));
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(testClusterUser, testClusterPass));

                builder.setHttpClientConfigCallback(hcb -> {
                    hcb.setDefaultCredentialsProvider(credentialsProvider);
                    if (testCluster.equals(DEFAULT_TEST_CLUSTER)) {
                        hcb.setSSLContext(SSLUtils.yesSSLContext());
                    }
                    return hcb;
                });

                client = builder.build();
                testClusterRunning();
            }
        } catch (Exception e) {
            staticLogger.error("Error", e);
            assumeFalse(true, "Elasticsearch does not seem to run.");
        }
    }

    @AfterAll
    public static void stopClient() throws IOException {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    protected void executeBefore(RestClient client) throws IOException {
        // Do nothing
    }

    @BeforeEach @AfterEach
    public void cleanIndex() throws IOException {
        if (indexName() != null) {
            deleteIndex(indexName());
        }
        for (String otherTestIndex : otherTestIndices()) {
            deleteIndex(otherTestIndex);
        }
        deleteIndex("twitter");
        executeBefore(client);
    }

    private void deleteIndex(String indexName) throws IOException {
        try {
            logger.debug("Removing index {}", indexName);
            client.performRequest(new Request("DELETE", "/" + indexName));
            logger.debug("Removing index {} - ok", indexName);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
            logger.debug("Removing index {} - not found", indexName);
        }
    }

    /**
     * Overwrite if twitter is not the index name. Can be null if no index should be created
     * @return Index name. Could be null
     */
    protected String indexName() {
        return "twitter";
    }

    /**
     * Overwrite if the test is using multiple indices. Can be null if no other index should be created
     * @return Index name. Could be null
     */
    protected List<String> otherTestIndices() {
        return new ArrayList<>();
    }
}
