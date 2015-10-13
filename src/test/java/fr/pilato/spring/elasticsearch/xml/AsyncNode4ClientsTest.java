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
import java.util.Map;


public class AsyncNode4ClientsTest extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/async-node-4clients/async-node-4clients-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Test
    public void test_node_client() throws Exception {
        Node node = ctx.getBean(Node.class);
        Assert.assertTrue(AopUtils.isAopProxy(node));

        Map<String,Client> clientMap = ctx.getBeansOfType(Client.class);

        for (Map.Entry<String,Client> entry : clientMap.entrySet()){
            if (entry.getKey().contains("async")){
                Assert.assertNotNull(Proxy.getInvocationHandler(entry.getValue()));
            }else{
                try {
                    Proxy.getInvocationHandler(entry.getValue());
                    throw new Exception("Must not be proxyfied");
                } catch (IllegalArgumentException e) {
                }
            }
        }
        //wait
        node.isClosed();
    }
}
