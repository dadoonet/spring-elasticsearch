# Spring factories for Elasticsearch

Welcome to the Spring factories for [Elasticsearch](https://www.elastic.co/elasticsearch/) project.

The factory provides a [High Level Rest Client for Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)
and automatically create index settings and templates based on what is found in the classpath:

* `/es/_index_lifecycles/` for [index lifecycles policies](#index-lifecycles-policies)
* `/es/INDEXNAME/_settings.json` for [index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/INDEXNAME/_update_settings.json` to [update existing index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/_component_templates/` for [component templates](#component-templates)
* `/es/_index_templates/` for [index templates](#index-templates)
* `/es/_templates/` for [legacy index templates](#templates-deprecated)
* `/es/_pipelines/` for [ingest pipelines](#ingest-pipelines)
* `/es/_aliases.json` for [aliases](#aliases)

## Documentation

* For 7.x elasticsearch versions, you are reading the latest documentation.
* For 6.x elasticsearch versions, look at [es-6.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-6.x).
* For 5.x elasticsearch versions, look at [es-5.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-5.x).
* For 2.x elasticsearch versions, look at [es-2.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-2.x).
* For 1.x elasticsearch versions, look at [es-1.4 branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-1.4).
* For 0.x elasticsearch versions, look at [0.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/0.x).

| spring-elasticsearch | elasticsearch |    Spring    | Release date |
|:--------------------:|:-------------:|:------------:|:------------:|
|     7.2-SNAPSHOT     |  7.0 - 7.x    |  5.3.15      |              |
|         7.1          |  7.0 - 7.x    |    5.3.15    |  2022-01-13  |
|         7.0          |  7.0 - 7.x    |    5.3.8     |  2021-06-21  |
|         6.7          |  6.7 - 6.x    |    5.1.3     |  2019-04-13  |
|         6.2          |  6.0 - 6.x    |    5.1.3     |  2019-01-08  |
|         6.1          |  6.0 - 6.x    |    5.0.7     |  2018-07-22  |
|         6.0          |  6.0 - 6.x    |    5.0.3     |  2018-02-08  |
|         5.0          |  5.0 - 5.x    |    4.3.10    |  2018-02-04  |
|        2.2.0         |  2.0 - 2.4    |    4.2.3     |  2017-03-09  |
|        2.1.0         |  2.0, 2.1     |    4.2.3     |  2015-11-25  |
|        2.0.0         |      2.0      |    4.1.4     |  2015-10-25  |
|        1.4.2         |     < 2.0     |    4.1.4     |  2015-03-03  |
|        1.4.1         |      1.4      |    4.1.4     |  2015-02-28  |
|        1.4.0         |      1.4      |    4.1.4     |  2015-01-03  |
|        1.3.0         |      1.3      |    4.0.6     |  2014-09-01  |
|        1.0.0         |      1.0      |    3.2.2     |  2014-02-14  |

## Build Status

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.pilato.spring/spring-elasticsearch/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/fr.pilato.spring/spring-elasticsearch/)
[![Build Status](https://github.com/dadoonet/spring-elasticsearch/actions/workflows/maven.yml/badge.svg)](https://github.com/dadoonet/spring-elasticsearch/actions/workflows/maven.yml)

## Release notes

### Changes in 7.x

* Nothing yet ;)

### Changes in 7.1

* Update to Beyonder 7.16 which brings in support for index lifecycles. 
You can add your index lifecycles policies in the `_index_lifecycles` dir.

### Major (breaking) changes in 7.0

* The `TransportClient` has been removed.

* As in Elasticsearch 7.x, only one single type is supported, you need to provide the mapping within
  the index settings ( `_settings.json` file). As a consequence:
 * `forceMapping` setting has been replaced by `forceIndex`.
 * `mappings` setting has been replaced by `indices`.
 * `mergeMapping` setting has been removed.

* `forceTemplate` setting has been removed. A template should be always updated.

* `_template` dir has been deprecated by `_templates` dir.

## Getting Started

### Maven dependency

Import spring-elasticsearch in you project `pom.xml` file:

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>7.1</version>
</dependency>
```

If you want to set a specific version of the High Level Rest client, add it to your `pom.xml` file:

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.16.2</version>
</dependency>
```

If you want to try out the most recent SNAPSHOT version [deployed on Sonatype](https://oss.sonatype.org/content/repositories/snapshots/fr/pilato/spring/spring-elasticsearch/):

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>7.2-SNAPSHOT</version>
</dependency>
```

Don't forget to add if needed the following repository in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>oss-snapshots</id>
        <name>Sonatype OSS Snapshots</name>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

If you depend on an elasticsearch SNAPSHOT version, you need to add the following repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>elastic-snapshots</id>
        <name>Elastic Snapshots</name>
        <url>http://snapshots.elastic.co/maven/</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

### Logger

We are using [slf4j](http://www.slf4j.org/) for logging but you have to provide the logging implementation
you want to use and bind it.

For example for this project we are using for tests [log4j2](http://logging.apache.org/log4j/). 
If you want to do so, add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-1.2-api</artifactId>
    <version>2.17.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>2.17.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.17.1</version>
</dependency>
```

## Using Java Annotations

Let's say you want to use Spring Java Annotations, here is a typical application you can build.

`pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.pilato.tests</groupId>
    <artifactId>spring-elasticsearch-test</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>fr.pilato.spring</groupId>
            <artifactId>spring-elasticsearch</artifactId>
            <version>7.1</version>
        </dependency>
    </dependencies>
</project>
```

`App.java`:

```java
package fr.pilato.tests;

import fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestApp {

    @Configuration
    public class AppConfig {
        @Bean
        public RestHighLevelClient esClient() throws Exception {
            ElasticsearchRestClientFactoryBean factory = new ElasticsearchRestClientFactoryBean();
            factory.setEsNodes(new String[]{"http://127.0.0.1:9200"});

            // Begin: If you are running with x-pack
            Properties props = new Properties();
            props.setProperty("xpack.security.user", "elastic:changeme");
		    factory.setProperties(props);
            // End: If you are running with x-pack

            factory.afterPropertiesSet();
            return factory.getObject();
        }
    }

    @Autowired
    private RestHighLevelClient client;

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("fr.pilato.tests");
        context.refresh();

        RestApp p = context.getBean(RestApp.class);
        p.run();

        context.close();
    }

    private void run() throws IOException {
        // Run a High Level request
        client.info(RequestOptions.DEFAULT);
        // You still have access to the Low Level client
        client.getLowLevel().performRequest(new Request("GET", "/"));
    }
}
```

## Features

The factory provides a [High Level Rest Client for Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)
and automatically create index settings and templates based on what is found in the classpath:

* `/es/INDEXNAME/_settings.json` for [index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/INDEXNAME/_update_settings.json` to [update existing index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/_component_templates/` for [component templates](#component-templates)
* `/es/_index_templates/` for [index templates](#index-templates)
* `/es/_templates/` for [legacy index templates](#templates-deprecated)
* `/es/_pipelines/` for [ingest pipelines](#ingest-pipelines)
* `/es/_aliases.json` for [aliases](#aliases)
* `/es/_index_lifecycles/` for [index lifecycles policies](#index-lifecycles-policies)

### Autoscan

By default, the factory will scan the classpath inside the default `/es` directory.
You can disable the autoscan and then provide manually every name for indices, templates...

```java
ElasticsearchRestClientFactoryBean factory = new ElasticsearchRestClientFactoryBean();
factory.setAutoscan(false);
factory.setIndices(new String[]{"twitter"});
```

### Default directory

You can change the default directory from `/es` to something else. The factory will look into this
directory to find the indices and the settings for the indices, templates...

```java
ElasticsearchRestClientFactoryBean factory = new ElasticsearchRestClientFactoryBean();
factory.setClasspathRoot("/foo");
```

### Indices

If you add in your classpath a file named `/es/twitter/_settings.json`, it will be automatically applied to define
settings for your `twitter` index.

For example, create the following file `src/main/resources/es/twitter/_settings.json` in your project:

```json
{
  "settings" : {
    "number_of_shards" : 3,
    "number_of_replicas" : 2
  },
  "mappings": {
    "properties" : {
      "message" : {"type" : "text", "store" : "yes"}
    }
  }
}
```

If you need to update settings for an existing index, let say `twitter`, add a file named  `/es/twitter/_update_settings.json` 
in your classpath. The factory will detect it and will try to update the settings:

```json
{
  "index" : {
    "number_of_replicas" : 1
  }
}
```

If you want to remove the existing indices every time the factory starts, you can use the `forceIndex` option:

```java
ElasticsearchRestClientFactoryBean factory = new ElasticsearchRestClientFactoryBean();
// Be careful: IT WILL REMOVE ALL EXISTING DATA FOR THE MANAGED INDICES.
factory.setForceIndex(true);
```

Be careful: **IT WILL REMOVE ALL EXISTING DATA** FOR THE MANAGED INDICES.


### Component templates

This feature will call the [Component Templates APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-component-template.html).
It's very common to use it with [index templates](#index-templates).

Let say you want to create a component template named `component1`. Just create a file named
`/es/_component_templates/component1.json`:

```json
{
  "template": {
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date"
        }
      }
    }
  }
}
```

Let say you want to create a component template named `component2`. Just create a file named
`/es/_component_templates/component2.json`:

```json
{
  "template": {
    "mappings": {
      "runtime": {
        "day_of_week": {
          "type": "keyword",
          "script": {
            "source": "emit(doc['@timestamp'].value.dayOfWeekEnum.getDisplayName(TextStyle.FULL, Locale.ROOT))"
          }
        }
      }
    }
  }
}
```

You can use then the 2 component templates in an index template as shown below.

### Index templates

This feature will call the [Index Templates APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-templates.html).
It can be used with [component templates](#component-templates).

Let say you want to create an index template named `template_1`. Just create a file named 
`/es/_index_templates/template_1.json`:

```json
{
  "index_patterns": ["te*", "bar*"],
  "template": {
    "settings": {
      "number_of_shards": 1
    },
    "mappings": {
      "_source": {
        "enabled": true
      },
      "properties": {
        "host_name": {
          "type": "keyword"
        },
        "created_at": {
          "type": "date",
          "format": "EEE MMM dd HH:mm:ss Z yyyy"
        }
      }
    },
    "aliases": {
      "mydata": { }
    }
  },
  "priority": 500,
  "composed_of": ["component1", "component2"],
  "version": 3,
  "_meta": {
    "description": "my custom"
  }
}
```

Note that this index template is using the 2 component templates that have been defined in the previous section.

### Templates (deprecated)

[Templates](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates-v1.html) have been 
deprecated by Elasticsearch. You should now use [Index Templates](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-templates.html) 
instead. With this factory, you can look at [Index Templates](#index-templates) to
use the new implementation.

Sometimes it's useful to define a template mapping that will automatically be applied to new indices created.

For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define a template named `twitter_template`, you have to define a file named `/es/_templates/twitter_template.json` in your project:

```json
{
    "index_patterns" : "twitter*",
    "settings" : {
        "number_of_shards" : 1
    },
    "mappings" : {
        "properties" : {
            "message" : {
                "type" : "text",
                "store" : "yes"
            }
        }
    }
}
```

### Aliases

An alias is helpful to define or remove an alias to a given index. You could also use an [index templates](#index-templates)
to do that automatically when at index creation time, but you can also define a file `/es/_aliases.json`:

```json
{
  "actions" : [
    { "remove": { "index": "test_1", "alias": "test" } },
    { "add":  { "index": "test_2", "alias": "test" } }
  ]
}
```

When the factory starts, it will automatically send the content to the [Aliases API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html)
and move the alias `test` from index `test_1` to index `test_2`.

### Ingest Pipelines

This feature will call the [Ingest Pipelines APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/ingest.html)

Let say you want to create an ingest pipeline named `pipeline1`. Just create a file named
`/es/_pipeline/pipeline1.json`:

```json
{
  "description": "My optional pipeline description",
  "processors": [
    {
      "set": {
        "description": "My optional processor description",
        "field": "my-long-field",
        "value": 10
      }
    },
    {
      "set": {
        "description": "Set 'my-boolean-field' to true",
        "field": "my-boolean-field",
        "value": true
      }
    },
    {
      "lowercase": {
        "field": "my-keyword-field"
      }
    }
  ]
}
```

### Index Lifecycles Policies

This feature will call the [Index Lifecycle APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-lifecycle-management.html).

Let say you want to create a policy named `policy1`. Just create a file named
`/es/_index_lifecycles/policy1.json`:

```json
{
  "policy": {
    "phases": {
      "warm": {
        "min_age": "10d",
        "actions": {
          "forcemerge": {
            "max_num_segments": 1
          }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

It will be automatically loaded into elasticsearch when you start the factory.
If you want to apply this policy to your index, you can define the following settings for the index in
`/es/twitter/_settings.json`:

```json
{
	"settings" : {
		"index.lifecycle.name": "policy1"
	}
}
```

## Using XML (deprecated)

### Using elasticsearch spring namespace for XML files

In your spring context file, just add namespaces like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:elasticsearch="http://www.pilato.fr/schema/elasticsearch"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		http://www.pilato.fr/schema/elasticsearch http://www.pilato.fr/schema/elasticsearch/elasticsearch-7.0.xsd">
</beans>
```

Then, you can get a REST High Level Client instance with:

```xml
<elasticsearch:rest-client id="esClient" />
```

### Using convention over configuration

By default, the factory will find every mapping file located under `es` directory.
So, if you have a mapping file named `/es/twitter/_doc.json` in your classpath, it will be automatically used by
the factory without defining anything:

```xml
<elasticsearch:rest-client id="esClient" />
```

You can disable this automatic lookup by setting the `autoscan` property to `false`:

```xml
<elasticsearch:rest-client id="esClient" autoscan="false" indices="twitter" />
```

### Changing classpath search path for mapping and settings files

By default, the factory look in `es` classpath folder to find if there is any of the files which are
used by the factory.
If you need to change it, you can use the `classpathRoot` property:

```xml
<elasticsearch:rest-client id="esClient" classpathRoot="myownfolder" />
```

So, if a `myownfolder/twitter/_settings.json` file exists in your classpath, it will be used by the factory.

### Define a rest client bean

In your spring context file, just define a client like this:

```xml
<elasticsearch:rest-client id="esClient" />
```

By default, you will get an [Elasticsearch High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)
connected to an Elasticsearch node already running at `http://localhost:9200`.

You can set the nodes you want to connect to:

```xml
<elasticsearch:rest-client id="esClient" esNodes="http://localhost:9200,http://localhost:9201" />
```

### Injecting the rest client in your java project

You can use the rest client in your java classes.

```java
import org.elasticsearch.client.RestHighLevelClient;

RestHighLevelClient client = ctx.getBean("esClient", RestHighLevelClient.class);
```

Better, you should use `@Autowired` annotation.

```java
// Inject your client...
@Autowired RestHighLevelClient client;
```

### Connecting to a secured X-Pack cluster

You need to define the `xpack.security.user` property as follows:

```
<util:properties id="esProperties">
    <prop key="xpack.security.user">elastic:changeme</prop>
</util:properties>

<elasticsearch:rest-client id="esClient" properties="esProperties" />
```

### Asynchronous initialization

Client bean initialization is by default synchronously. It can be initialized asynchronously with the attributes `async` and `taskExecutor`.

```xml
<task:executor pool-size="4" id="taskExecutor"/>
<elasticsearch:rest-client id="esClient" async="true" taskExecutor="taskExecutor"/>
```
Asynchronous initialization does not block Spring startup but it continues on background on another thread.
Any methods call to these beans before elasticsearch is initialized will be blocked. `taskExecutor` references a standard Spring's task executor.

### Managing indices

If you want to manage indices at startup (creating missing indices and applying optional settings):

```xml
<elasticsearch:rest-client id="esClient"
                           indices="twitter" />
```

This will create an [Elasticsearch High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html)
and will create an index `twitter`.

If you need to manage more than one index, just use a comma separated list:

```xml
<elasticsearch:rest-client id="esClient" 
                           indices="twitter,facebook" />
```

If you need to update settings for an existing `twitter` index, add a file named  
`/es/twitter/_update_settings.json` in your classpath. The factory will detect it and will try to update 
settings unless you explicitly set `mergeSettings` to `false`.

```xml
<elasticsearch:rest-client id="esClient" mergeSettings="false" />
```

If updating the settings fails, the factory will not start.

For test purpose or for continuous integration, you could force the factory to clean the previous `indices` when starting the client.
It will *remove all your datas* for every index which has been defined. Just set  `forceIndex` property to `true`.

```xml
<elasticsearch:rest-client id="esClient" forceIndex="true" />
```

### Creating aliases to indices

When creating an index, it could be useful to add an alias on it.
For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define an alias named twitter, you can use the `aliases` property:

```xml
<elasticsearch:rest-client id="esClient" 
                           aliases="twitter:twitter2012,twitter:twitter2013,twitter:twitter2014" />
```

### Creating templates (deprecated)

If you are not using autoscan, you can use the `templates` property to define the templates:

```xml
<!--
    We add also a facebook_template template just for showing how to
    define more than one template...
-->
<elasticsearch:rest-client id="esClient" 
                           templates="twitter_template,facebook_template" />
```

### Creating ingest pipelines

If you are not using autoscan, you can use the `pipelines` property to define the templates:

```xml
<elasticsearch:rest-client id="esClient"
                           pipelines="pipeline1,pipeline2" />
```

## Old fashion bean definition

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

    <!-- The Rest Client -->
    <bean id="esRestClient" class="fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean" >
        <property name="esNodes">
            <list>
                <value>http://localhost:9200</value>
                <value>http://localhost:9201</value>
            </list>
        </property>

        <property name="autoscan" value="false" />
        <property name="indices">
            <list>
                <value>twitter</value>
            </list>
        </property>
        <property name="classpathRoot" value="myownfolder" />
        <property name="forceIndex" value="true" />
        <property name="mergeSettings" value="true" />
        <property name="componentTemplates">
            <list>
                <value>component1</value>
                <value>component2</value>
            </list>
        </property>
        <property name="indexTemplates">
            <list>
                <value>template_1</value>
            </list>
        </property>
        <property name="templates">
            <list>
                <value>twitter_template</value>
            </list>
        </property>
        <property name="pipelines">
            <list>
                <value>pipeline1</value>
            </list>
        </property>
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

# Thanks

Special thanks to

- [Nicolas Huray](https://github.com/nhuray) for his contribution about
[templates](https://github.com/dadoonet/spring-elasticsearch/pull/4)
- [Nicolas Labrot](https://github.com/nithril) for his contribution about
[async](https://github.com/dadoonet/spring-elasticsearch/pull/30)

# Running tests

If you want to run tests (integration tests) from your IDE, you need to start first an elasticsearch instance.
Tests are expecting a node running at `localhost:9200`.

To run the tests using Maven (on the CLI), just run:

```sh
mvn clean install
```

# Release guide

To release the project you need to run the release plugin with the `release` profile as you need to sign the artifacts:

```sh
mvn release:prepare
git push --tags
git push
mvn release:perform -Prelease
```

If you need to skip the tests, run:

```sh
mvn release:perform -Prelease -Darguments="-DskipTests"
```

If everything is ok in https://s01.oss.sonatype.org/#stagingRepositories, you can perform the release with:

```sh
mvn nexus-staging:release
mvn nexus-staging:drop
```

To announce the release, run:

```sh
cd target/checkout
# Run the following command if you want to check the announcement email
mvn changes:announcement-generate
cat target/announcement/announcement.vm

# Announce the release (change your smtp username and password)
mvn changes:announcement-mail -Dchanges.username='YourSmtpUserName' -Dchanges.password='YourSmtpUserPassword'
```

# License

This software is licensed under the Apache 2 license, quoted below.

	Copyright 2011-2022 David Pilato
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may not
	use this file except in compliance with the License. You may obtain a copy of
	the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	License for the specific language governing permissions and limitations under
	the License.
