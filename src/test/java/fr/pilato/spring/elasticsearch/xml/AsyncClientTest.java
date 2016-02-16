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

package fr.pilato.spring.elasticsearch.xml;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class AsyncClientTest extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/async-client/async-client-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Test
    public void test_node_client() throws Exception {
        Node node = ctx.getBean(Node.class);
        try {
            Proxy.getInvocationHandler(node);
            throw new Exception("Must not be proxyfied");
        } catch (IllegalArgumentException e) {
        }

        Client client = checkClient(true);
        client.admin().cluster().prepareState().execute().get();

        // #92: prepareSearch() errors with async created TransportClient
        client.prepareIndex("index", "type").setSource("foo", "bar").setRefresh(true).get();
        SearchResponse response = client.prepareSearch("index").get();
        assertThat(response.getHits().getTotalHits(), is(1L));
    }
}
