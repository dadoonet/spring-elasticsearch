package fr.pilato.spring.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link FactoryBean} implementation used to create a {@link Client} element
 * from a {@link Node}.
 * @author David Pilato
 * @see ElasticsearchAbstractClientFactoryBean
 * @see ElasticsearchNodeFactoryBean
 */
public class ElasticsearchClientFactoryBean extends ElasticsearchAbstractClientFactoryBean {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired Node node;

	/**
	 * Node to get a client from
	 * @param node An Elasticsearch node
	 */
	public void setNode(Node node) {
		this.node = node;
	}
	
	@Override
	protected Client buildClient() throws Exception {
		if (node == null)
			throw new Exception(
					"You must define an ElasticSearch Node as a Spring Bean.");
		return node.client();
	}
}
