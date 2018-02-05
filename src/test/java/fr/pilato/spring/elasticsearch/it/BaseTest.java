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
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeThat;

public abstract class BaseTest {
    private final static Logger staticLogger = ESLoggerFactory.getLogger(BaseTest.class);
    public static boolean securityInstalled;
    protected final Logger logger = ESLoggerFactory.getLogger(this.getClass().getName());

    private static RestClient client;
    public final static String testCredentials = System.getProperty("tests.cluster.credentials", "elastic:changeme");

    private static void startRestClient() throws IOException {
        if (client == null) {
            client = RestClient.builder(new HttpHost("127.0.0.1", 9200)).build();

            securityInstalled = testClusterRunning(false);
            if (securityInstalled) {
                // We have a secured cluster. So we need to create a secured client
                // But first we need to close the previous client we built
                if (client != null) {
                    client.close();
                }

                String[] split = testCredentials.split(":");
                if (split.length < 2) {
                    throw new IllegalArgumentException("tests.cluster.credentials must have the form username:password");
                }
                String username = split[0];
                String password = split[1];

                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

                client = RestClient.builder(new HttpHost("127.0.0.1", 9200))
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                        .build();
                securityInstalled = testClusterRunning(true);
            }
        }
    }

    private static boolean testClusterRunning(boolean withSecurity) throws IOException {
        try {
            Response response = client.performRequest("GET", "/");
            Map<String, Object> result = new ObjectMapper().readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});

            Map<String, Object> asMap = (Map<String, Object>) result.get("version");

            staticLogger.info("Starting integration tests against an external cluster running elasticsearch [{}] with {}",
                    asMap.get("number"), withSecurity ? "security" : "no security" );
            return withSecurity;
        } catch (ConnectException e) {
            // If we have an exception here, let's ignore the test
            staticLogger.warn("Integration tests are skipped: [{}]", e.getMessage());
            assumeThat("Integration tests are skipped", e.getMessage(), not(containsString("Connection refused")));
            return withSecurity;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 401) {
                staticLogger.debug("The cluster is secured. So we need to build a client with security", e);
                return true;
            } else {
                staticLogger.error("Full error is", e);
                throw e;
            }
        } catch (IOException e) {
            staticLogger.error("Full error is", e);
            throw e;
        }
    }


    @BeforeClass
    public static void testElasticsearchIsRunning() {
        try {
            startRestClient();
        } catch (Exception e) {
            assumeNoException(e);
        }
    }

    @AfterClass
    public static void stopClient() throws IOException {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Before
    public void cleanIndex() throws IOException {
        if (indexName() != null) {
            try {
                client.performRequest("DELETE", indexName());
            } catch (ResponseException e) {
                if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                    throw e;
                }
            }
        }
        try {
            client.performRequest("DELETE", "twitter");
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
        }
    }

    /**
     * Overwrite if twitter is not the index name. Can be null if no index should be created
     * @return Index name. Could be null
     */
    protected String indexName() {
        return "twitter";
    }
}
