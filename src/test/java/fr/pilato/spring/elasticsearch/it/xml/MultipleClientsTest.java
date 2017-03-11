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

package fr.pilato.spring.elasticsearch.it.xml;

import org.elasticsearch.client.Client;
import org.junit.Test;


public class MultipleClientsTest extends AbstractXmlContextModel {
    private final String[] xmlBeans = {"models/multiple-clients/multiple-clients-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Override
    public String indexName() {
        return "twitter";
    }

    @Test
	public void test_multiple_clients() {
		Client client = checkClient("esClient");
        Client client2 = checkClient("esClient2");

        // We test how many shards and replica we have
        // We don't expect the number of replicas to be 4 as we won't merge _update_settings.json
        // See #31: https://github.com/dadoonet/spring-elasticsearch/issues/31
        assertShardsAndReplicas(client, "twitter", 5, 1);

        // Let's do the same thing with the second client
        // We test how many shards and replica we have
        // We don't expect the number of replicas to be 4 as we won't merge _update_settings.json
        // See #31: https://github.com/dadoonet/spring-elasticsearch/issues/31
        assertShardsAndReplicas(client2, "twitter", 5, 1);
    }
}
