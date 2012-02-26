package fr.pilato.spring.elasticsearch.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.transport.TransportAddress;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ElasticsearchTransportClientNamespaceTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-transport-client-namespace-test-context.xml");
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
		
		TransportAddress transportAddress = adresses.get(0);
		assertFalse("We should not find default configuration here.", transportAddress.match("inet[localhost:9300]"));
		assertEquals("URL should be myhostname:9399", "inet[myhostname:9399]", transportAddress.toString());
	}
}
