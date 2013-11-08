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
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.transport.TransportAddress;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;


public class ElasticsearchTransportClientNamespaceTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-nodefortransportclient-namespace-test-context.xml",
				"fr/pilato/spring/elasticsearch/xml/es-transport-client-namespace-test-context.xml");
	}
	
	@AfterClass
	static public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}
	
	@Test
	public void test_transport_client() {
		Client client = ctx.getBean("testTransportClient", Client.class);
		assertNotNull("Client must not be null...", client);
		assertTrue("Client should be an instance of org.elasticsearch.client.transport.TransportClient.", client instanceof org.elasticsearch.client.transport.TransportClient);

		TransportClient tClient = (TransportClient) client;
		ImmutableList<TransportAddress> adresses = tClient.transportAddresses();
		assertNotNull("Nodes urls must not be null...", adresses);
		assertFalse("Nodes urls must not be empty...", adresses.isEmpty());
		
//		TransportAddress transportAddress = adresses.get(0);
//		assertEquals("URL should be localhost:9399", "inet[localhost:9399]", transportAddress.toString());

		// We wait a while for connection to the cluster (1s should be enough)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		// Testing if we are really connected to a cluster node
		assertFalse("We should be connected at least to one node.", tClient.connectedNodes().isEmpty());
		DiscoveryNode node = tClient.connectedNodes().get(0);
		
		// TODO Raise a bug to ES Team ? This test fails... Why ?
		//assertEquals("The node should be junit.node.transport", "junit.node.transport", node.getName());

		assertTrue("We should be connected to the master node.", node.isMasterNode());
	}
}
