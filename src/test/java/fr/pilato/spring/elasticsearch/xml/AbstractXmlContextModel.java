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

import fr.pilato.spring.elasticsearch.BaseTest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Proxy;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractXmlContextModel extends BaseTest {
    protected ConfigurableApplicationContext ctx;

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

    protected Client checkClient() {
        return checkClient(null, null);
    }

    protected Client checkClient(Boolean async) {
        return checkClient(null, async);
    }

    protected Client checkClient(String name) {
        return checkClient(name, null);
    }

    protected Client checkClient(String name, Boolean async) {
        Client client;

        if (name != null) {
            client = ctx.getBean(name, Client.class);
        } else {
            client = ctx.getBean(Client.class);
        }
        if (async != null) {
            if (async) {
                assertThat(Proxy.getInvocationHandler(client), not(nullValue()));
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

    protected Node checkNode() {
        return checkNode(null, false);
    }

    protected Node checkNode(boolean async) {
        return checkNode(null, async);
    }

    protected Node checkNode(String name) {
        return checkNode(name, false);
    }

    protected Node checkNode(String name, boolean async) {
        Node node;

        if (name != null) {
            node = ctx.getBean(name, Node.class);
        } else {
            node = ctx.getBean(Node.class);
        }
        if (async) {
            assertThat(Proxy.getInvocationHandler(node), not(nullValue()));
        }
        assertThat(node, not(nullValue()));
        return node;
    }

    protected boolean isMappingExist(Client client, String index, String type) {
        IndexMetaData imd = null;
        try {
            ClusterState cs = client.admin().cluster().prepareState().setIndices(index).execute().actionGet().getState();
            imd = cs.getMetaData().index(index);
        } catch (IndexMissingException e) {
            // If there is no index, there is no mapping either
        }

        if (imd == null) return false;

        MappingMetaData mdd = imd.mapping(type);

        if (mdd != null) return true;
        return false;
    }
}
