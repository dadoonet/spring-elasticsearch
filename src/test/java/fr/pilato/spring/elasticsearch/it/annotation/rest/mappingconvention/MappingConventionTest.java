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

package fr.pilato.spring.elasticsearch.it.annotation.rest.mappingconvention;

import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;


public class MappingConventionTest extends AbstractRestAnnotationContextModel {

    @Override
    protected void checkUseCaseSpecific(RestHighLevelClient client) throws IOException {
        Map<String, Object> response = runRestQuery(client.getLowLevelClient(), "/twitter/_mapping", "twitter", "mappings", "properties");
        assertThat(response, hasKey("message"));
    }
}
