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

import fr.pilato.spring.elasticsearch.ElasticsearchHighLevelRestClientFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

class HighLevelRestClientBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String id = XMLParserUtil.getElementStringValue(element, "id");
		String name = XMLParserUtil.getElementStringValue(element, "name");

		String client = XMLParserUtil.getElementStringValue(element, "client");

		BeanDefinition highLevelClient;

        GenericBeanDefinition bdef = new GenericBeanDefinition();
        bdef.setBeanClass(ElasticsearchHighLevelRestClientFactoryBean.class);
		BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(ElasticsearchHighLevelRestClientFactoryBean.class);
		if (client != null && client.length() > 0) {
			nodeFactory.addPropertyReference("client", client);
		}

		highLevelClient = nodeFactory.getBeanDefinition();

		// Register BeanDefinition
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, highLevelClient);
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, highLevelClient);
		}
		
		return bdef;
	}
}
