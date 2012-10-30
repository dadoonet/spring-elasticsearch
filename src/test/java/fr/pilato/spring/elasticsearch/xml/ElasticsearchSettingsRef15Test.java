package fr.pilato.spring.elasticsearch.xml;

import org.elasticsearch.node.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for feature request #15 : https://github.com/dadoonet/spring-elasticsearch/issues/15
 * @author David
 *
 */
public class ElasticsearchSettingsRef15Test {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es15/es-settings15-test-context.xml");
	}
	
	@AfterClass
	static public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}
	
	@Test
	public void test_node_clustername() {
		Node node = ctx.getBean("esNode", Node.class);
		assertNotNull("Node must not be null...", node);
        assertEquals("newclustername", node.settings().get("cluster.name"));
	}
}
