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

package fr.pilato.spring.elasticsearch.it.xml.transport;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MappingTest extends AbstractXmlContextModel {
    private final String[] xmlBeans = {"models/transport/mapping/mapping-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    protected void checkUseCaseSpecific(Client client) {
        checkClient("esClient2");

        MappingMetaData response = client.admin().indices().prepareGetMappings().get().getMappings().get("twitter").get("tweet");
        String mapping = new String(response.source().uncompressed());
        // This one comes from the first mapping
        assertThat(mapping, containsString("message"));
        // This one comes from the second mapping
        assertThat(mapping, containsString("author"));
    }
}
