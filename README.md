Spring factories for Elasticsearch
==================================

Welcome to the Spring factories for [Elasticsearch](http://www.elasticsearch.org/) project.


Versions
--------

| spring-elasticsearch | ElasticSearch |
|:--------------------:|:-------------:|
|   master (0.0.3)     |    0.20.0.RC1 |
|        0.0.2         |    0.19.4     |
|        0.0.1         |    0.19.4     |

Note that you can use 0.0.2 released version with Elasticsearch 0.19.5 and above (tested with 0.20.0.RC1).


Build Status
------------

Thanks to cloudbees for the [build status](https://buildhive.cloudbees.com): [![Build Status](https://buildhive.cloudbees.com/job/dadoonet/job/spring-elasticsearch/badge/icon)](https://buildhive.cloudbees.com/job/dadoonet/job/spring-elasticsearch/)

Getting Started
---------------

### Maven dependency

Import spring-elasticsearch in you project `pom.xml` file:

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>0.0.2</version>
</dependency>
```

If you want to set a specific version of elasticsearch, add it to your `pom.xml` file:

```xml
<dependency>
  <groupId>org.elasticsearch</groupId>
  <artifactId>elasticsearch</artifactId>
  <version>0.20.0.RC1</version>
</dependency>
```

### Using elasticsearch spring namespace for XML files

In your spring context file, just add namespaces like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:util="http://www.springframework.org/schema/util"
	xmlns:elasticsearch="http://www.pilato.fr/schema/elasticsearch"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
		http://www.pilato.fr/schema/elasticsearch http://www.pilato.fr/schema/elasticsearch/elasticsearch-0.1.xsd">
</beans>
```

### Define a client Transport bean

In your spring context file, just define a client like this:

```xml
<elasticsearch:client id="esClient" />
```
    
By default, you will get an [Elasticsearch Transport Client](http://www.elasticsearch.org/guide/reference/java-api/client.html) connected to an Elasticsearch node already running at `localhost:9300`.


### Define a node and get a node client bean

In your spring context file, just define a node like this:

```xml
<elasticsearch:node id="esNode" />
```

By default, it will build an [Elasticsearch Node](http://www.elasticsearch.org/guide/reference/modules/node.html) running at `localhost:9300`.

Then, you can ask the node to give you a client.

```xml
<elasticsearch:client node="esNode" id="esClient" />
```
  
You will get an [Elasticsearch Node Client](http://www.elasticsearch.org/guide/reference/java-api/client.html).


### Injecting client in your java project

Now, you can use the client (either the node) in your java classes.

```java
import org.elasticsearch.client.Client;

Client client = ctx.getBean("esClient", Client.class);
```

Better, you should use `@Autowired` annotation.

```java
 // if you really need it and have started a node using the factory
@Autowired Node node;

// Inject your client...
@Autowired Client client;
```

Elasticsearch properties
------------------------

You can set your elasticsearch properties in a file:

```xml
<bean id="esClient"
     class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
     <property name="settingsFile" value="es.properties" />
</bean>
```

By default, the factory will use the es.properties file if it exists in your classpath.


Transport Client Network settings
---------------------------------

You can (you should) define your nodes settings when using a transport client:

```xml
<bean id="esClient"
     class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
     <property name="esNodes">
       <list>
         <value>localhost:9300</value>
         <value>localhost:9301</value>
       </list>
     </property>
</bean>
```

Bean properties
---------------

### Managing indexes and types

If you want to manage indexes and types at startup (creating missing indexes/types and applying mappings):

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="mappings">
		<list>
			<value>twitter/tweet</value>
		</list>
	</property>
</bean>
```

This will create an [Elasticsearch Client](http://www.elasticsearch.org/guide/reference/java-api/client.html) that will check
when starting that index `twitter` exists and `tweet` type is defined.

If you add in your classpath a file named `es/myindex/_settings.json`, it will be automatically applied to define
settings for your `myindex` index.

For example, create the following file `src/main/resources/es/twitter/_settings.json` in your project:

```javascript
{
  "index" : {
    "number_of_shards" : 3,
    "number_of_replicas" : 2
  }
}
```

Also, if you define a file named `es/myindex/mytype.json`, it will be automatically applied as the mapping for the `mytype` type in the `myindex` index.

For example, create the following file `src/main/resources/es/twitter/tweet.json` in your project:

```javascript
{
  "tweet" : {
    "properties" : {
      "message" : {"type" : "string", "store" : "yes"}
    }
  }
}
```

### Using convention over configuration

By default, the factory will find every mapping file located under `es` directory.
So, if you have a mapping file named `es/twitter/tweet.json` in your classpath, it will be automatically used by the factory
without defining anything:

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" />
```

You can disable this automatic lookup by setting the `autoscan` property to `false`:

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean">
	<property name="autoscan" value="false" />
	<property name="mappings">
		<list>
			<value>twitter/tweet</value>
		</list>
	</property>
</bean>
```

### Creating aliases to indexes

When creating an index, it could be useful to add an alias on it.
For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define an alias named twitter, you can use the `aliases` property:

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="aliases">
		<list>
			<value>twitter:twitter2012</value>
			<value>twitter:twitter2013</value>
			<value>twitter:twitter2014</value>
		</list>
	</property>
</bean>
```

### Creating templates

Sometimes it's useful to define a template mapping that will automatically be applied to new indices created. 

For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define a template named `twitter_template`, you can use the `templates` property:

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="templates">
		<list>
			<value>twitter_template</value>
		</list>
	</property>
</bean>
```

To configure your template you have to define a file named `es/_template/twitter_template.json` in your project:

```javascript
{
    "template" : "twee*",
    "settings" : {
        "number_of_shards" : 1
    },
    "mappings" : {
        "tweet" : {
            "properties" : {
                "message" : {
                    "type" : "string",
                    "store" : "yes"
                }
            }
        }
    }
}
```

### Changing classpath search path for mapping and settings files

By default, the factory look in `es` classpath folder to find if there is index settings or mappings definitions.
If you need to change it, you can use the `classpathRoot` property:

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="mappings">
		<list>
			<value>twitter/tweet</value>
		</list>
	</property>
	<property name="classpathRoot" value="myownfolder" />
</bean>
```

So, if a `myownfolder/twitter/_settings.xml` file exists in your classpath, it will be used by the factory.

### Merge mappings

If you need to merge mapping for an existing `type`, set  `mergeMapping` property to `true`.

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="mappings">
		<list>
			<value>twitter/tweet</value>
		</list>
	</property>
	<property name="mergeMapping" value="true" />
</bean>
```

If merging fails, the factory will not start ([BeanCreationException](https://github.com/SpringSource/spring-framework/blob/master/spring-beans/src/main/java/org/springframework/beans/factory/BeanCreationException.java)
 will be raised with a 
 [MergeMappingException](https://github.com/elasticsearch/elasticsearch/blob/master/src/main/java/org/elasticsearch/index/mapper/MergeMappingException.java) cause).

### Merge settings

If you need to merge settings for an existing `index`, set  `mergeSettings` property to `true`.

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="mappings">
		<list>
			<value>twitter/tweet</value>
		</list>
	</property>
	<property name="mergeSettings" value="true" />
</bean>
```

If merging fails, the factory will not start.


### Force rebuild mappings (use with caution)

For test purpose or for continuous integration, you could force the factory to clean the previous `type` when starting the client.
It will *remove all your datas* for that `type`. Just set  `forceMapping` property to `true`.

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="mappings">
		<list>
			<value>twitter/tweet</value>
		</list>
	</property>
	<property name="forceMapping" value="true" />
</bean>
```

### Force rebuild templates (use with caution)

For test purpose or for continuous integration, you could force the factory to clean the previous `template` when starting the client.
Just set  `forceTemplate` property to `true`.

```xml
<bean id="esClient"
    class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
	<property name="templates">
		<list>
			<value>twitter_template</value>
		</list>
	</property>
	<property name="forceTemplate" value="true" />
</bean>
```

Thanks
------

Special thanks to [Nicolas Huray](https://github.com/nhuray) for his contribution about [templates](https://github.com/dadoonet/spring-elasticsearch/pull/4)


License
-------

This software is licensed under the Apache 2 license, quoted below.

	Copyright 2011-2013 David Pilato
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may not
	use this file except in compliance with the License. You may obtain a copy of
	the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	License for the specific language governing permissions and limitations under
	the License.
