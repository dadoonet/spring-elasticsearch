package fr.pilato.spring.elasticsearch;

import static org.junit.Assert.*;

import org.elasticsearch.client.Client;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticsearchNodeClientTest extends AbstractESTest {
	@Autowired Client client;
	
	@Test
	public void testStart() {
		assertNotNull(client);
	}
}
