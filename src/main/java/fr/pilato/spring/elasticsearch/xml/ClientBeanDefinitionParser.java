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

package fr.pilato.spring.elasticsearch.xml;

import fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

class ClientBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String id = XMLParserUtil.getElementStringValue(element, "id");
		String name = XMLParserUtil.getElementStringValue(element, "name");

		String esNodes = XMLParserUtil.getElementStringValue(element, "esNodes");
		String plugins = XMLParserUtil.getElementStringValue(element, "plugins");

        String properties = XMLParserUtil.getElementStringValue(element, "properties");

        boolean forceMapping = XMLParserUtil.getElementBooleanValue(element, "forceMapping");
        boolean forceTemplate = XMLParserUtil.getElementBooleanValue(element, "forceTemplate");
        boolean mergeMapping = XMLParserUtil.getElementBooleanValue(element, "mergeMapping");
        boolean mergeSettings = XMLParserUtil.getElementBooleanValue(element, "mergeSettings");
        boolean autoscan = XMLParserUtil.getElementBooleanValue(element, "autoscan", true);

        String classpathRoot = XMLParserUtil.getElementStringValue(element, "classpathRoot");
        String mappings = XMLParserUtil.getElementStringValue(element, "mappings");
        String aliases = XMLParserUtil.getElementStringValue(element, "aliases");
        String templates = XMLParserUtil.getElementStringValue(element, "templates");

		String taskExecutor = XMLParserUtil.getElementStringValue(element, "taskExecutor");
		String async = XMLParserUtil.getElementStringValue(element, "async");

        // Checking bean definition
		if (esNodes == null || esNodes.length() == 0) {
		    // We fallback to default value
            esNodes = "127.0.0.1:9300";
		}
		
		BeanDefinition client;

        GenericBeanDefinition bdef = new GenericBeanDefinition();
        bdef.setBeanClass(ElasticsearchTransportClientFactoryBean.class);
        BeanDefinitionBuilder clientBuilder = startClientBuilder(ElasticsearchTransportClientFactoryBean.class,
                properties, forceMapping, forceTemplate, mergeMapping, mergeSettings, autoscan,
                classpathRoot, mappings, aliases, templates, async, taskExecutor);
        client = ClientBeanDefinitionParser.buildTransportClientDef(clientBuilder, esNodes, plugins);

		// Register NodeBeanDefinition
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, client);	
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, client);
		}
		
		return bdef;
	}

    /**
     * Managing common properties for TransportClient
     */
    private static BeanDefinitionBuilder startClientBuilder(Class beanClass, String properties,
                                                            boolean forceMapping, boolean forceTemplate,
                                                            boolean mergeMapping, boolean mergeSettings,
                                                            boolean autoscan, String classpathRoot, String mappings,
                                                            String aliases, String templates, String async, String taskExecutor) {
        BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        if (properties != null && properties.length() > 0) {
            nodeFactory.addPropertyReference("properties", properties);
        }
        nodeFactory.addPropertyValue("forceMapping", forceMapping);
        nodeFactory.addPropertyValue("forceTemplate", forceTemplate);
        nodeFactory.addPropertyValue("mergeMapping", mergeMapping);
        nodeFactory.addPropertyValue("mergeSettings", mergeSettings);
        nodeFactory.addPropertyValue("autoscan", autoscan);
        if (classpathRoot != null && classpathRoot.length() > 0) {
            nodeFactory.addPropertyValue("classpathRoot", classpathRoot);
        }
        if (mappings != null && mappings.length() > 0) {
            nodeFactory.addPropertyValue("mappings", mappings);
        }
        if (aliases != null && aliases.length() > 0) {
            nodeFactory.addPropertyValue("aliases", aliases);
        }
        if (templates != null && templates.length() > 0) {
            nodeFactory.addPropertyValue("templates", templates);
        }

		if (async != null && async.length() > 0) {
			nodeFactory.addPropertyValue("async", async);
		}
		if (taskExecutor != null && taskExecutor.length() > 0) {
			nodeFactory.addPropertyReference("taskExecutor", taskExecutor);
		}


        return nodeFactory;
    }

	private static BeanDefinition buildTransportClientDef(BeanDefinitionBuilder nodeFactory, String esNodes, String plugins) {
		if (esNodes != null && esNodes.length() > 0) {
			nodeFactory.addPropertyValue("esNodes", esNodes);	
		}
        if (plugins != null && plugins.length() > 0) {
            nodeFactory.addPropertyValue("plugins", plugins);
        }
        return nodeFactory.getBeanDefinition();
	}

}
