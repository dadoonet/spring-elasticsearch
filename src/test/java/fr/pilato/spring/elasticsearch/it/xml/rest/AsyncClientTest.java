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

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.http.entity.ContentType;
import org.elasticsearch.client.http.entity.StringEntity;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class AsyncClientTest extends AbstractXmlContextModel {
    private final String[] xmlBeans = {"models/rest/async-client/async-client-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Test
    public void testFactoriesCreated() throws ExecutionException, InterruptedException, IOException {
        RestClient client = checkClient(null, true);

        // Just check that everything runs fine
        runRestQuery(client, "/");

        // #92: prepareSearch() errors with async created TransportClient
        client.performRequest("POST", "/twitter/tweet", Collections.singletonMap("refresh", "true"),
                new StringEntity("{\"foo\":\"bar\"}", ContentType.APPLICATION_JSON));
        Map<String, Object> hits = runRestQuery(client, "/twitter/_search", "hits");
        assertThat(hits.get("total"), is(1));
    }
}
