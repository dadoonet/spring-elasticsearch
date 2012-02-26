package fr.pilato.spring.elasticsearch.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;

public class NodeBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		GenericBeanDefinition bdef = new GenericBeanDefinition();
		bdef.setBeanClass(ElasticsearchNodeFactoryBean.class);
		
		String id = element.getAttribute("id");
		String name = element.getAttribute("name");
		String settingsFile = element.getAttribute("settingsFile");
		
		// Define NodeBeanDefinition
		BeanDefinition node = NodeBeanDefinitionParser.buildNodeDef(settingsFile);

		// Register NodeBeanDefinition
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, node);	
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, node);
		}
		
		return bdef;
	}

	public static BeanDefinition buildNodeDef(String settingsFile) {
		BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(ElasticsearchNodeFactoryBean.class);
		if (settingsFile != null && settingsFile.length() > 0) {
			nodeFactory.addPropertyValue("settingsFile", settingsFile);	
		} 
		return nodeFactory.getBeanDefinition();
	}

}
