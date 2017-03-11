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

package fr.pilato.spring.elasticsearch.it.xml;

import fr.pilato.spring.elasticsearch.it.BaseTest;
import fr.pilato.spring.elasticsearch.proxy.GenericInvocationHandler;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.transport.TransportAddress;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractXmlContextModel extends BaseTest {
    private ConfigurableApplicationContext ctx;

    /**
     * @return list of xml files needed to be loaded for this test
     */
    abstract String[] xmlBeans();

    @Before
    public void startContext() {
        String[] xmlBeans = xmlBeans();
        if (xmlBeans.length == 0) {
            fail("Can not start a factory without any context!");
        }
        logger.info("  --> Starting Spring Context with [{}] file(s)", xmlBeans.length);
        for (String xmlBean : xmlBeans) {
            logger.info("    - {}", xmlBean);
        }
        ctx = new ClassPathXmlApplicationContext(xmlBeans);
    }

    @After
    public void stopContext() {
        if (ctx != null) {
            logger.info("  --> Closing Spring Context");
            ctx.close();
        }
    }

    Client checkClient() {
        return checkClient(null, null);
    }

    Client checkClient(String name) {
        return checkClient(name, null);
    }

    Client checkClient(String name, Boolean async) {
        Client client;

        if (name != null) {
            client = ctx.getBean(name, Client.class);
        } else {
            client = ctx.getBean(Client.class);
        }
        if (async != null) {
            if (async) {
                assertThat(client, instanceOf(Advised.class));
                assertThat(((Advised) client).getAdvisors()[0].getAdvice() , instanceOf(GenericInvocationHandler.class));
            } else {
                try {
                    Proxy.getInvocationHandler(client);
                    fail("Must not be proxyfied");
                } catch (IllegalArgumentException e) {
                    // We expect that
                }
            }
        }
        assertThat(client, not(nullValue()));
        return client;
    }

    boolean isMappingExist(Client client, String index, String type) {
        ClusterState cs = client.admin().cluster().prepareState().setIndices(index).get().getState();
        IndexMetaData imd = cs.getMetaData().index(index);

        if (imd == null) return false;

        MappingMetaData mdd = imd.mapping(type);

        return mdd != null;
    }

    void assertShardsAndReplicas(Client client, String indexName, int expectedShards, int expectedReplicas) {
        ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
        assertThat(response.getState().getMetaData().getIndices().get(indexName).getNumberOfShards(), is(expectedShards));
        assertThat(response.getState().getMetaData().getIndices().get(indexName).getNumberOfReplicas(), is(expectedReplicas));
    }

    void assertTransportClient(Client client) {
        assertThat(client, CoreMatchers.instanceOf(TransportClient.class));

        TransportClient tClient = (TransportClient) client;
        List<TransportAddress> addresses = tClient.transportAddresses();
        assertThat(addresses, not(emptyCollectionOf(TransportAddress.class)));
        assertThat(addresses.size(), is(1));
    }
}
