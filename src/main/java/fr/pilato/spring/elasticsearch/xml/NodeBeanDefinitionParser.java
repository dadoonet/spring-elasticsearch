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

import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class NodeBeanDefinitionParser implements BeanDefinitionParser {
    protected static final Logger logger = LoggerFactory.getLogger(NodeBeanDefinitionParser.class);

    public BeanDefinition parse(Element element, ParserContext parserContext) {
		GenericBeanDefinition bdef = new GenericBeanDefinition();
		bdef.setBeanClass(ElasticsearchNodeFactoryBean.class);
		
		String id = element.getAttribute("id");
		String name = element.getAttribute("name");
		String settingsFile = element.getAttribute("settingsFile");
        String properties = element.getAttribute("properties");
		String taskExecutor = element.getAttribute("taskExecutor");
		String async = element.getAttribute("async");

        // Define NodeBeanDefinition
		BeanDefinition node = NodeBeanDefinitionParser.buildNodeDef(settingsFile, properties, async, taskExecutor);

		// Register NodeBeanDefinition
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, node);	
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, node);
		}
		
		return bdef;
	}

	public static BeanDefinition buildNodeDef(String settingsFile, String properties, String async, String taskExecutor) {
		BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(ElasticsearchNodeFactoryBean.class);
		if (settingsFile != null && settingsFile.length() > 0) {
            logger.warn("settingsFile is deprecated. Use properties attribute instead. See issue #15: https://github.com/dadoonet/spring-elasticsearch/issues/15.");
			nodeFactory.addPropertyValue("settingsFile", settingsFile);
		}
        if (properties != null && properties.length() > 0) {
            nodeFactory.addPropertyReference("properties", properties);
        }
		if (async != null && async.length() > 0) {
			nodeFactory.addPropertyValue("async", async);
		}
		if (taskExecutor != null && taskExecutor.length() > 0) {
			nodeFactory.addPropertyReference("taskExecutor", taskExecutor);
		}

        return nodeFactory.getBeanDefinition();
	}

}
