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

package fr.pilato.spring.elasticsearch.it.annotation.rest.aliases;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

public class AliasesTest extends AbstractRestAnnotationContextModel {

    @Override
    protected List<String> otherTestIndices() {
        return asList("test_1", "test_2");
    }

    @Override
    protected void checkUseCaseSpecific(ElasticsearchClient client) throws Exception {
        Map<String, IndexAliases> response = client.indices().getAlias(gar -> gar.name("test")).result();
        assertThat(response, not(hasKey("test_1")));
        assertThat(response, hasKey("test_2"));
    }
}
