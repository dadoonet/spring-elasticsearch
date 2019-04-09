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

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;


public class MultipleClientsTest extends AbstractXmlContextModel {
    private final String[] xmlBeans = {"models/rest/multiple-clients/multiple-clients-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    protected void checkUseCaseSpecific(RestHighLevelClient client) throws IOException {
        RestHighLevelClient client2 = checkClient("esClient2");
        // We test how many shards and replica we have
        // Let's do the same thing with the second client
        // We test how many shards and replica we have
        // We don't expect the number of replicas to be 4 as we won't merge _update_settings.json
        // See #31: https://github.com/dadoonet/spring-elasticsearch/issues/31
        assertShardsAndReplicas(client2.getLowLevelClient(), "twitter", 1, 1);
    }
}
