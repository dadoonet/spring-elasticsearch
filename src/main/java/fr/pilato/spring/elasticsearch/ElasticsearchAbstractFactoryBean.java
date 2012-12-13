/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
