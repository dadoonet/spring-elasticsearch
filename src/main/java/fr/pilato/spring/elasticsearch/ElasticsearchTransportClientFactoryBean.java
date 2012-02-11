package fr.pilato.spring.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.FactoryBean;

/**
 * An {@link FactoryBean} used to create an ElasticSearch Transport {@link Client}.
 * <br>
 * You need to define the nodes you want to communicate with.<br>
 * Don't forget to create an es.properties file if you want to set specific values
 * for this client, e.g.: cluster.name
 * <br>Example :
 * <pre>
 * {@code
 *  <bean id="esClient"
 *    class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
 *    <property name="esNodes">
 *      <list>
 *        <value>localhost:9300</value>
 *        <value>localhost:9301</value>
 *      </list>
 *    </property>
 *  </bean>
 * }
 * </pre>
 * @see TransportClient
 * @author David Pilato
 */
public class ElasticsearchTransportClientFactoryBean extends ElasticsearchAbstractClientFactoryBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private String[] esNodes =  { "localhost:9300" };
	
	/**
	 * Define ES nodes to communicate with.
	 * @return An array of nodes hostname:port
	 */
	public String[] getEsNodes() {
		return esNodes;
	}
	
	/**
	 * Define ES nodes to communicate with.
	 * <br/>use : hostname:port form
	 * <p>Example :<br/>
 	 * <pre>
	 * {@code
	 * <property name="esNodes">
     *  <list>
     *   <value>localhost:9300</value>
     *   <value>localhost:9301</value>
     *  </list>
     * </property>
	 * }
	 * </pre>
	 * If not set, default to [ "localhost:9300" ].
	 * <br>If port is not set, default to 9300.
	 * @param esNodes An array of nodes hostname:port
	 */
	public void setEsNodes(String[] esNodes) {
		this.esNodes = esNodes;
	}
	
	@Override
	protected Client buildClient() throws Exception {
		Settings settings = ImmutableSettings.settingsBuilder()
				.loadFromClasspath(this.settingsFile)
				.build();

		TransportClient client = new TransportClient(settings);

		for (int i = 0; i < esNodes.length; i++) {
			client.addTransportAddress(toAddress(esNodes[i]));
		}

		return client;
	}
	
	/**
	 * Helper to define an hostname and port with a String like hostname:port
	 * @param address Node address hostname:port (or hostname)
	 * @return
	 */
	private InetSocketTransportAddress toAddress(String address) {
		if (address == null) return null;
		
		String[] splitted = address.split(":");
		int port = 9300;
		if (splitted.length > 1) {
			port = Integer.parseInt(splitted[1]);
		}
		
		return new InetSocketTransportAddress(splitted[0], port);
	}
	
}
