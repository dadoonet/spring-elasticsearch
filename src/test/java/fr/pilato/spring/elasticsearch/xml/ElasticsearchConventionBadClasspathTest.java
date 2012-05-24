package fr.pilato.spring.elasticsearch.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ElasticsearchConventionBadClasspathTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-convention-badclasspath-test-context.xml");
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

		// We wait a while for connection to the cluster (1s should be enough)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		assertTrue("tweet type should not exist in twitter index", !isMappingExist(client, "twitter", "tweet"));
		
	}
	
	private boolean isMappingExist(Client client, String index, String type) {
		ClusterState cs = client.admin().cluster().prepareState().setFilterIndices(index).execute().actionGet().getState();
		IndexMetaData imd = cs.getMetaData().index(index);
		if (imd == null) return false;
		
		MappingMetaData mdd = imd.mapping(type);
		
		if (mdd != null) return true;
		return false;
	}

}
