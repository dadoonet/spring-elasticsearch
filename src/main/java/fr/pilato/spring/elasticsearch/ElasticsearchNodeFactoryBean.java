package fr.pilato.spring.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A {@link FactoryBean} implementation used to create a {@link Node} element
 * which is an embedded instance of the cluster within a running application.
 * <p>
 * The lifecycle of the underlying {@link Node} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link Node#close()}
 * @see ElasticsearchAbstractFactoryBean
 * @author David Pilato
 */
public class ElasticsearchNodeFactoryBean extends ElasticsearchAbstractFactoryBean 
	implements FactoryBean<Node>,
		InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Node node;

	@Override
	public void afterPropertiesSet() throws Exception {
		final NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();

		if (null != settings) {
			nodeBuilder.getSettings().put(settings);
		}

		if (null != settingsFile) {
			Settings settings = ImmutableSettings.settingsBuilder()
					.loadFromClasspath(this.settingsFile)
					.build();
			nodeBuilder.getSettings().put(settings);
		}


		if (logger.isDebugEnabled()) logger.debug("Starting ElasticSearch node...");
		node = nodeBuilder.node();
		logger.info( "Node [" + node.settings().get("name") + "] for [" + node.settings().get("cluster.name") + "] cluster started..." );
		if (logger.isDebugEnabled()) logger.debug( "  - data : " + node.settings().get("path.data") );
		if (logger.isDebugEnabled()) logger.debug( "  - logs : " + node.settings().get("path.logs") );
	}

	@Override
	public void destroy() throws Exception {
		try {
			logger.info("Closing ElasticSearch node " + node.settings().get("name") );
			node.close();
		} catch (final Exception e) {
			logger.error("Error closing Elasticsearch node: ", e);
		}
	}

	@Override
	public Node getObject() throws Exception {
		return node;
	}

	@Override
	public Class<Node> getObjectType() {
		return Node.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
