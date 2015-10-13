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

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutionException;


public class AsyncNodeClientTest extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/async-node-client/async-node-client-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Test
    public void test_node_client() throws ExecutionException, InterruptedException {
        Node node = ctx.getBean(Node.class);
        Assert.assertTrue(AopUtils.isAopProxy(node));
        Client client = checkClient("testNodeClient");
        Assert.assertNotNull(Proxy.getInvocationHandler(client));
        client.admin().cluster().prepareState().execute().get();
    }
}
