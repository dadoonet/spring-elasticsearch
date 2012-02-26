package fr.pilato.spring.elasticsearch.xml;

import static org.junit.Assert.*;

import org.elasticsearch.node.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ElasticsearchNodeNamespaceTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-namespace-test-context.xml");
	}
	
	@AfterClass
	static public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}
	
	@Test
	public void test_simple_node() {
		Node node = ctx.getBean("testNode", Node.class);
		assertNotNull("Node must not be null...", node);
		assertEquals("Cluster name is incorrect. Settings are not loaded.", "junit.cluster", node.settings().get("cluster.name"));
	}
	
	@Test
	public void test_node_settings() {
		Node node = ctx.getBean("testNodeSettings", Node.class);
		assertNotNull("Node must not be null...", node);
		assertEquals("Cluster name is incorrect. Settings are not loaded.", "junit.cluster.xml", node.settings().get("cluster.name"));
	}
}
