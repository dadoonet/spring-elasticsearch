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

package fr.pilato.spring.elasticsearch.it.annotation.rest.indextemplates;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class IndexTemplatesTest extends AbstractRestAnnotationContextModel {

    @Override
    protected void executeBefore(RestClient client) throws IOException {
        try {
            client.performRequest(new Request("DELETE", "/_index_template/template_1"));
        } catch (ResponseException ignored) { }
        try {
            client.performRequest(new Request("DELETE", "/_component_template/component1"));
        } catch (ResponseException ignored) { }
        try {
            client.performRequest(new Request("DELETE", "/_component_template/component2"));
        } catch (ResponseException ignored) { }
    }

    @Override
    protected String indexName() {
        return null;
    }

    protected void checkUseCaseSpecific(ElasticsearchClient client) throws IOException {
        boolean existsTemplate = client.indices().existsIndexTemplate(eit -> eit.name("template_1")).value();
        assertThat(existsTemplate, is(true));
    }
}
