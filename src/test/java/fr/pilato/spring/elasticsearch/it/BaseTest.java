/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import static org.junit.Assume.assumeNoException;

public abstract class BaseTest {
    protected Logger logger = ESLoggerFactory.getLogger(this.getClass().getName());

    protected static TransportClient client;

    @BeforeClass
    public static void testElasticsearchIsRunning() {
        try {
            client = new PreBuiltTransportClient(Settings.builder().put("client.transport.ignore_cluster_name", true).build())
                    .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));
            client.admin().cluster().prepareHealth().setTimeout(TimeValue.timeValueMillis(200)).get();
        } catch (Exception e) {
            assumeNoException(e);
        }
    }

    @AfterClass
    public static void stopClient() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Before
    public void cleanIndex() {
        if (indexName() != null) {
            try {
                client.admin().indices().delete(new DeleteIndexRequest(indexName())).get();
            } catch (ExecutionException | InterruptedException | IndexNotFoundException ignore) {
            }
        }
    }

    public String indexName() {
        return null;
    }
}
