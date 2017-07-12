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

import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.junit.Assume.assumeNoException;

public abstract class BaseTest {
    protected final Logger logger = ESLoggerFactory.getLogger(this.getClass().getName());

    private static RestClient client;

    @BeforeClass
    public static void testElasticsearchIsRunning() {
        try {
            client = RestClient.builder(new HttpHost("127.0.0.1", 9200)).build();
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
