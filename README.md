Spring factories for Elasticsearch
==================================

Welcome to the Spring factories for [Elasticsearch](http://www.elasticsearch.org/) project.

Actually, since version 1.4.1, this project has been split in two parts:

* [Elasticsearch Beyonder](https://github.com/dadoonet/elasticsearch-beyonder/) which find resources in
project classpath to automatically create indices, types and templates.
* This project which is building Client and Node beans using Spring.

The Spring specific part of this project will basically move to
[spring-data-elasticsearch](https://github.com/spring-projects/spring-data-elasticsearch) project.


Versions
--------

* For 1.x elasticsearch versions, look at [master branch](https://github.com/dadoonet/spring-elasticsearch/tree/master).
* For 0.x elasticsearch versions, look at [0.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/0.x).

|   spring-elasticsearch  | elasticsearch |   Spring     | Release date |
|:-----------------------:|:-------------:|:------------:|:------------:|
|           1.4.0         |      1.4      |    4.1.4     |  2015-01-03  |
|           1.3.0         |      1.3      |    4.0.6     |  2014-09-01  |
|           1.0.0         |      1.0      |    3.2.2     |  2014-02-14  |

Build Status
------------

Thanks to cloudbees for the [build status](https://buildhive.cloudbees.com):
[![Build Status](https://buildhive.cloudbees.com/job/dadoonet/job/spring-elasticsearch/badge/icon)](https://buildhive.cloudbees.com/job/dadoonet/job/spring-elasticsearch/)


Getting Started
---------------

### Maven dependency

Import spring-elasticsearch in you project `pom.xml` file:

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>1.4.0</version>
</dependency>
```

If you want to set a specific version of elasticsearch, add it to your `pom.xml` file:

```xml
<dependency>
  <groupId>org.elasticsearch</groupId>
  <artifactId>elasticsearch</artifactId>
  <version>1.4.2</version>
</dependency>
```

### Using elasticsearch spring namespace for XML files

In your spring context file, just add namespaces like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:util="http://www.springframework.org/schema/util"
	xmlns:elasticsearch="http://www.pilato.fr/schema/elasticsearch"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd
        http://www.pilato.fr/schema/elasticsearch http://www.pilato.fr/schema/elasticsearch/elasticsearch-0.3.xsd">
</beans>
```

### Define a client Transport bean

In your spring context file, just define a client like this:

```xml
<elasticsearch:client id="esClient" />
```
    
By default, you will get an [Elasticsearch Transport Client](http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html)
connected to an Elasticsearch node already running at `localhost:9300` using `elasticsearch` as cluster name.

You can set the nodes you want to connect to:

```xml
<elasticsearch:client id="esClient" esNodes="localhost:9300,localhost:9301" />
```

**Important notes**

> Note that you should define the same clustername as the one you defined on your running nodes.
> Otherwise, your Transport Client won't connect to the node. See [Elasticsearch properties](#elasticsearch-properties).

> Note also that you must define the transport client port (9300-9399) and not the REST port (9200-9299).
> Transport client does not use REST API.

### Define a node and get a node client bean

In your spring context file, just define a node like this:

```xml
<elasticsearch:node id="esNode" />
```

By default, it will build an [Elasticsearch Node](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-node.html)
running at `localhost:9300`.

Then, you can ask the node to give you a client.

```xml
<elasticsearch:client node="esNode" id="esClient" />
```
  
You will get an [Elasticsearch Node Client](http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html).


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

You can define your client properties using a property file such as:

```properties
cluster.name=myclustername
```

And load it in Spring context:

```xml
<util:properties id="esproperties"
    location="classpath:fr/pilato/spring/elasticsearch/xml/esclient-transport.properties"/>
```

Note that you can also define properties as follow:

```xml
<util:map id="esProperties">
    <entry key="cluster.name" value="newclustername"/>
</util:map>
```

<elasticsearch:client id="esClient" esNodes="localhost:9300,localhost:9301"
    properties="esproperties"/>

Injecting properties in node and client is now easy:

```xml
<elasticsearch:node id="esNode" properties="esproperties" />
<elasticsearch:client id="esClient" properties="esproperties" />
```

Transport Client Properties
---------------------------

You can (you should) define your nodes settings when using a transport client:

```xml
<elasticsearch:client id="esClient" esNodes="localhost:9300,localhost:9301" />
```

Node Client Properties
----------------------

You can define your running node from which you want to get a client:

```xml
<elasticsearch:node id="esNode" properties="esproperties" />
<elasticsearch:client id="esClient" node="esNode" />
```


Common Client properties
------------------------

For both TransportClient and NodeClient, you can define many properties to manage automatic creation
of index, mappings, templates and aliases.

### Managing indexes and types

If you want to manage indexes and types at startup (creating missing indexes/types and applying mappings):

```xml
<elasticsearch:client id="esClient" node="esNode"
    mappings="twitter/tweet" />
```

This will create an [Elasticsearch Client](http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html) that will check
when starting that index `twitter` exists and `tweet` type is defined.

If you need to manage more than one type or index, just use a comma separated list:

```xml
<elasticsearch:client id="esClient" node="esNode"
    mappings="twitter/tweet,twitter/user,facebook/user" />
```

If you add in your classpath a file named `es/twitter/_settings.json`, it will be automatically applied to define
settings for your `twitter` index.

For example, create the following file `src/main/resources/es/twitter/_settings.json` in your project:

```javascript
{
  "index" : {
    "number_of_shards" : 3,
    "number_of_replicas" : 2
  }
}
```

Also, if you define a file named `es/twitter/tweet.json`, it will be automatically applied as the mapping for
the `tweet` type in the `twitter` index.

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
So, if you have a mapping file named `es/twitter/tweet.json` in your classpath, it will be automatically used by
the factory without defining anything:

```xml
<elasticsearch:client id="esClient" />
```

You can disable this automatic lookup by setting the `autoscan` property to `false`:

```xml
<elasticsearch:client id="esClient" autoscan="false" mappings="twitter/tweet" />
```

### Creating aliases to indexes

When creating an index, it could be useful to add an alias on it.
For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define an alias named twitter, you can use the `aliases` property:

```xml
<elasticsearch:client id="esClient"
    aliases="twitter:twitter2012,twitter:twitter2013,twitter:twitter2014" />
```

### Creating templates

Sometimes it's useful to define a template mapping that will automatically be applied to new indices created. 

For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define a template named `twitter_template`, you can use the `templates` property:

```xml
<!--
    We add also a facebook_template template just for showing how to
    define more than one template...
-->
<elasticsearch:client id="esClient"
    templates="twitter_template,facebook_template" />
```

To configure your template you have to define a file named `es/_template/twitter_template.json` in your project:

```javascript
{
    "template" : "twitter*",
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
<elasticsearch:client id="esClient" classpathRoot="myownfolder" />
```

So, if a `myownfolder/twitter/_settings.xml` file exists in your classpath, it will be used by the factory.

### Merge mappings

If you need to merge mapping for an existing `type`, set  `mergeMapping` property to `true`.

```xml
<elasticsearch:client id="esClient" mergeMapping="true" />
```

If merging fails, the factory will not start ([BeanCreationException](https://github.com/SpringSource/spring-framework/blob/master/spring-beans/src/main/java/org/springframework/beans/factory/BeanCreationException.java)
 will be raised with a 
 [MergeMappingException](https://github.com/elasticsearch/elasticsearch/blob/master/src/main/java/org/elasticsearch/index/mapper/MergeMappingException.java) cause).

### Merge settings

If you need to merge settings for an existing `index`, add a file named  `es/twitter/_update_settings.json` in your
classpath. The factory will detect it and will try to merge settings unless you explicitly set `mergeSettings` to `false`.

```xml
<elasticsearch:client id="esClient" mergeSettings="false" />
```

If merging fails, the factory will not start.


### Force rebuild mappings (use with caution)

For test purpose or for continuous integration, you could force the factory to clean the previous `type` when starting the client.
It will *remove all your datas* for that `type`. Just set  `forceMapping` property to `true`.

```xml
<elasticsearch:client id="esClient" forceMapping="true" />
```

### Force rebuild templates (use with caution)

For test purpose or for continuous integration, you could force the factory to clean the previous `template` when starting the client.
Just set  `forceTemplate` property to `true`.

```xml
<elasticsearch:client id="esClient" forceTemplate="true" />
```

### Asyncronous initialization

Node and client beans initialization are by default synchronously. They can be initialized asynchronously with the attributes `async` and `taskExecutor`.

```xml
<task:executor pool-size="4" id="taskExecutor"/>
<elasticsearch:client id="esClient" async="true" taskExecutor="taskExecutor"/>
```
Aynchronous initialization does not block Spring startup but it continues on background on another thread.
Any methods call to these beans before elasticsearch is initialized will be blocker. `taskExecutor` references a standard Spring's task executor.


Old fashion bean definition
---------------------------

Note that you can use the old fashion method to define your beans instead of using `<elasticsearch:...>` namespace:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:util="http://www.springframework.org/schema/util"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd">

    <util:map id="esproperties">
        <entry key="cluster.name" value="newclustername"/>
    </util:map>

    <bean id="esNode"
        class="fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean">
    </bean>

    <bean id="esClient" class="fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean" >
        <!-- If ElasticsearchTransportClientFactoryBean -->
        <!--
        <property name="esNodes">
            <list>
                <value>localhost:9300</value>
                <value>localhost:9301</value>
            </list>
        </property>
        -->

        <!--
            When using ElasticsearchClientFactoryBean running node is
            automatically injected. But you can define it as well.
        -->
        <property name="node" ref="esNode" />

        <property name="properties" ref="esproperties" />

        <property name="autoscan" value="false" />
        <property name="mappings">
            <list>
                <value>twitter/tweet</value>
            </list>
        </property>
        <property name="classpathRoot" value="myownfolder" />
        <property name="forceMapping" value="true" />
        <property name="mergeSettings" value="true" />
        <property name="templates">
            <list>
                <value>twitter_template</value>
            </list>
        </property>
        <property name="forceTemplate" value="true" />
        <property name="aliases">
            <list>
                <value>twitter:twitter2012</value>
                <value>twitter:twitter2013</value>
                <value>twitter:twitter2014</value>
            </list>
        </property>
    </bean>

</beans>
```

Thanks
------

Special thanks to

- [Nicolas Huray](https://github.com/nhuray) for his contribution about
[templates](https://github.com/dadoonet/spring-elasticsearch/pull/4)
- [Nicolas Labrot](https://github.com/nithril‎) for his contribution about
[async](https://github.com/dadoonet/spring-elasticsearch/pull/30)


License
-------

This software is licensed under the Apache 2 license, quoted below.

	Copyright 2011-2015 David Pilato
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may not
	use this file except in compliance with the License. You may obtain a copy of
	the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	License for the specific language governing permissions and limitations under
	the License.
