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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.indices.GetIndicesSettingsResponse;
import fr.pilato.spring.elasticsearch.it.annotation.AbstractAnnotationContextModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class AbstractRestAnnotationContextModel extends AbstractAnnotationContextModel {
    protected ElasticsearchClient checkClient(String name) {
        ElasticsearchClient client;

        if (name != null) {
            client = ctx.getBean(name, ElasticsearchClient.class);
        } else {
            client = ctx.getBean(ElasticsearchClient.class);
        }
        assertThat(client, not(nullValue()));
        return client;
    }

    @Test
    public void testFactoriesCreated() throws Exception {
        ElasticsearchClient client = checkClient(beanName());
        checkUseCaseSpecific(client);

        // If an index is expected, let's check it actually exists
        if (indexName() != null) {
            // We test how many shards and replica we have
            assertShardsAndReplicas(client, indexName(), expectedShards(), expectedReplicas());

            // #92: search errors with async created Client
            client.index(ir -> ir.index("twitter").id("1").withJson(new StringReader("{\"foo\":\"bar\"}")));
            GetResponse<Void> response = client.get(gr -> gr.index("twitter").id("1"), Void.class);
            assertThat(response, notNullValue());
        }
    }

    /**
     * Overwrite it to implement use case specific tests
     * @param client Client representing bean named esClient
     */
    protected void checkUseCaseSpecific(ElasticsearchClient client) throws Exception {
    }

    protected void assertShardsAndReplicas(ElasticsearchClient client, String indexName, int expectedShards, int expectedReplicas) throws IOException {
        GetIndicesSettingsResponse settings = client.indices().getSettings(gisr -> gisr.index(indexName));
        assertThat(Integer.parseInt(settings.get(indexName).settings().index().numberOfShards()), is(expectedShards));
        assertThat(Integer.parseInt(settings.get(indexName).settings().index().numberOfReplicas()), is(expectedReplicas));
    }
}
