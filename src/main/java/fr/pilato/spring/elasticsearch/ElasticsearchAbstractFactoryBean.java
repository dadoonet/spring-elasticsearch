package fr.pilato.spring.elasticsearch;

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;

/**
 * An abstract {@link FactoryBean} used to create an ElasticSearch object.
 * <p>
 * By default, the factory will load a es.properties file in the classloader. It will
 * contains all information needed for your client, e.g.: cluster.name
 * <br>
 * If you want to  modify the filename used for properties, just define the settingsFile property.
 * @author David Pilato
 */
public abstract class ElasticsearchAbstractFactoryBean {

	protected String settingsFile = "es.properties";
	
	protected Map<String, String> settings;

	/**
	 * Elasticsearch Settings file classpath URL (default : es.properties)
	 * <p>Example :<br/>
 	 * <pre>
	 * {@code
	 * <property name="settingsFile" value="es.properties" />
	 * }
	 * </pre>
	 * @param settingsFile the settingsFile to set
	 * @see {@link #setSettings(Map)} to define settings in spring xml file
	 */
	public void setSettingsFile(String settingsFile) {
		this.settingsFile = settingsFile;
	}

	/**
	 * Embedded Elasticsearch settings
	 * <p>Example :<br/>
	 * 
	 * @param settings
	 * @see {@link #setSettingsFile(String)} to define settings in file
	 */
	public void setSettings(final Map<String, String> settings) {
		this.settings = settings;
	}
}
