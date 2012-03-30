package fr.pilato.spring.elasticsearch.xml;

import static org.junit.Assert.assertNotNull;

import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ElasticsearchMappingTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-mapping-test-context.xml");
	}
	
	@AfterClass
	static public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}
	
	@Test
	public void test_transport_client() {
		Client client = ctx.getBean("esClient", Client.class);
		assertNotNull("Client must not be null...", client);

		Client client2 = ctx.getBean("esClient2", Client.class);
		assertNotNull("Client2 must not be null...", client2);
		// We wait a while for connection to the cluster (1s should be enough)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
	}
}
