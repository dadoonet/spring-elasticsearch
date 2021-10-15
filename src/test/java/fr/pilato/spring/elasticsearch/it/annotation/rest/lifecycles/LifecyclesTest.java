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

package fr.pilato.spring.elasticsearch.it.annotation.rest.lifecycles;

import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class LifecyclesTest extends AbstractRestAnnotationContextModel {

    @Override
    protected void executeBefore(RestClient client) throws IOException {
        try {
            client.performRequest(new Request("DELETE", "/_ilm/policy/policy1"));
        } catch (ResponseException ignored) { }
    }

    protected void checkUseCaseSpecific(RestHighLevelClient client) throws IOException {
        assertThat(client.getLowLevelClient().performRequest(new Request("GET", "/_ilm/policy/policy1")).getStatusLine().getStatusCode(), is(200));
    }
}
