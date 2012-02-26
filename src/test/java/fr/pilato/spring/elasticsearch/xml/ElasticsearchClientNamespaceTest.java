package fr.pilato.spring.elasticsearch.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ElasticsearchClientNamespaceTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-client-namespace-test-context.xml");
	}
	
	@AfterClass
	static public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}
	
	@Test
	public void test_node_client() {
		Client client = ctx.getBean("testNodeClient", Client.class);
		assertNotNull("Client must not be null...", client);
		assertTrue("Client should be an instance of org.elasticsearch.client.node.NodeClient.", client instanceof org.elasticsearch.client.node.NodeClient);
	}
}
