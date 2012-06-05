package fr.pilato.spring.elasticsearch.annotation;

import junit.framework.Assert;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:annotation-context.xml"
		})
public class ConfigurationTest {
	
	@Autowired Node node;

	@Autowired Client client;

	@Test
	public void testNode() {
		Assert.assertNotNull(node);
		Assert.assertNotNull(client);
	}
	
}
