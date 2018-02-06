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

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class AsyncClientTest extends AbstractXmlContextModel {
    private final String[] xmlBeans = {"models/rest/async-client/async-client-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Test
    @Ignore
    public void testFactoriesCreated() throws IOException {
        RestHighLevelClient client = checkClient(null, true);

        // Just check that everything runs fine
        // TODO This call is working fine
        client.getLowLevelClient().performRequest("GET", "/");

        // TODO This call is producing NPE
        /*
            Logs are saying things like:
            INFO  [o.s.a.f.CglibAopProxy] Final method [public final org.elasticsearch.action.index.IndexResponse org.elasticsearch.client.RestHighLevelClient.index(org.elasticsearch.action.index.IndexRequest,org.apache.http.Header[]) throws java.io.IOException] cannot get proxied via CGLIB: Calls to this method will NOT be routed to the target instance and might lead to NPEs against uninitialized fields in the proxy instance.
         */
        client.info();

        // #92: prepareSearch() errors with async created TransportClient
        client.index(new IndexRequest("twitter", "tweet")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .source("{\"foo\":\"bar\"}", XContentType.JSON));
        SearchResponse searchResponse = client.search(new SearchRequest("twitter"));
        assertThat(searchResponse.getHits().totalHits, is(1));
    }
}
