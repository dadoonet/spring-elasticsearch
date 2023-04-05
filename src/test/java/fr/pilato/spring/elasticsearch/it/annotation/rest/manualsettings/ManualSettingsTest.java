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

package fr.pilato.spring.elasticsearch.it.annotation.rest.manualsettings;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ManualSettingsTest extends AbstractRestAnnotationContextModel {
    @Override
    protected void executeBefore(RestClient client) throws IOException {
        try {
            client.performRequest(new Request("DELETE", "/_index_template/template_1"));
        } catch (ResponseException ignored) { }
        try {
            client.performRequest(new Request("DELETE", "/_component_template/component1"));
        } catch (ResponseException ignored) { }
    }

    @Override
    protected void checkUseCaseSpecific(ElasticsearchClient client) throws Exception {
        // Test manual creation of one index even though we have multiple ones in the classpath
        assertThat(client.indices().get(gir -> gir.index("twitter")).result(), hasKey("twitter"));
        assertThat(client.indices().get(gir -> gir.index("foobar").ignoreUnavailable(true)).result(), not(hasKey("twitter")));

        // Test manual creation of one alias even though we have multiple ones in the classpath
        assertThat(client.indices().getAlias(gar -> gar.index("twitter")).result().get("twitter").aliases(), hasKey("alltheworld"));
        assertThat(client.indices().getAlias(gar -> gar.index("twitter")).result().get("twitter").aliases(), not(hasKey("alias")));

        // Test manual creation of one pipeline even though we have multiple ones in the classpath
        assertThat(client.ingest().getPipeline(gpr -> gpr.id("pipeline1")).get("pipeline1"), notNullValue());
        assertThat(client.ingest().getPipeline(gpr -> gpr.id("pipeline2")).get("pipeline2"), nullValue());

        // Test manual creation of one component template even though we have multiple ones in the classpath
        assertThat(client.cluster().getComponentTemplate(gctr -> gctr.name("component1")).componentTemplates(), hasSize(1));
        assertThrows(ElasticsearchException.class, () -> {
            try {
                client.cluster().getComponentTemplate(gctr -> gctr.name("component2"));
            } catch (ElasticsearchException e) {
                assertThat(e.status(), is(404));
                throw e;
            }
        });

        // Test manual creation of one index template even though we have multiple ones in the classpath
        assertThat(client.indices().existsIndexTemplate(eit -> eit.name("template_1")).value(), is(true));
        assertThat(client.indices().existsIndexTemplate(eit -> eit.name("template_2")).value(), is(false));
    }
}
