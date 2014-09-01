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

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class MultinodesNamespaceTest extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/multinodes-namespace/multinodes-namespace-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

 	@Test
	public void test_multiple_nodes() {
		Client client = checkClient("testNodeClient");
		assertThat(client, instanceOf(org.elasticsearch.client.transport.TransportClient.class));

        NodesInfoResponse nodeInfos = client.admin().cluster().prepareNodesInfo().get();
        assertThat(nodeInfos.getNodes().length, is(2));
	}
}
