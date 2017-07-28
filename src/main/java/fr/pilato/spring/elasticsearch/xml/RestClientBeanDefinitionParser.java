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

import fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static fr.pilato.spring.elasticsearch.xml.ClientBeanDefinitionParser.startClientBuilder;

class RestClientBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String id = XMLParserUtil.getElementStringValue(element, "id");
		String name = XMLParserUtil.getElementStringValue(element, "name");

		String esNodes = XMLParserUtil.getElementStringValue(element, "esNodes");

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
            esNodes = "127.0.0.1:9200";
		}
		
		BeanDefinition client;

        GenericBeanDefinition bdef = new GenericBeanDefinition();
        bdef.setBeanClass(ElasticsearchRestClientFactoryBean.class);
        BeanDefinitionBuilder clientBuilder = startClientBuilder(ElasticsearchRestClientFactoryBean.class,
                properties, forceMapping, forceTemplate, mergeMapping, mergeSettings, autoscan,
                classpathRoot, mappings, aliases, templates, async, taskExecutor);
        client = buildRestClientDef(clientBuilder, esNodes);

		// Register NodeBeanDefinition
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, client);	
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, client);
		}
		
		return bdef;
	}

	private static BeanDefinition buildRestClientDef(BeanDefinitionBuilder nodeFactory, String esNodes) {
		if (esNodes != null && esNodes.length() > 0) {
			nodeFactory.addPropertyValue("esNodes", esNodes);	
		}
        return nodeFactory.getBeanDefinition();
	}

}
