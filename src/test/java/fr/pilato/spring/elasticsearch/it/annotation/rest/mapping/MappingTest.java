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

package fr.pilato.spring.elasticsearch.it.annotation.rest.mapping;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;


public class MappingTest extends AbstractRestAnnotationContextModel {

    protected void checkUseCaseSpecific(ElasticsearchClient client) throws IOException {
        ElasticsearchClient client2 = checkClient("esClient2");

        Map<String, Property> response = client2.indices()
                .getMapping(gmr -> gmr.index("twitter")).get("twitter").mappings().properties();
        // This one comes from the first mapping
        assertThat(response, hasKey("message"));
        // This one comes from the second mapping
        assertThat(response, hasKey("author"));
    }
}
