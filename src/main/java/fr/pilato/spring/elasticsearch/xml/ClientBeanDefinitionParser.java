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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;

class ClientBeanDefinitionParser {

    /**
     * Managing common properties for TransportClient
     */
    static BeanDefinitionBuilder startClientBuilder(Class beanClass, String properties,
                                                    boolean forceIndex, boolean forceTemplate,
                                                    boolean mergeSettings,
                                                    boolean autoscan, String classpathRoot, String mappings,
                                                    String aliases, String templates, String async, String taskExecutor) {
        BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        if (properties != null && properties.length() > 0) {
            nodeFactory.addPropertyReference("properties", properties);
        }
        nodeFactory.addPropertyValue("forceIndex", forceIndex);
        nodeFactory.addPropertyValue("forceTemplate", forceTemplate);
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
}
