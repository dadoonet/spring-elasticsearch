# Spring factories for Elasticsearch

**This project is now archived.** 

Please use [Elasticsearch Beyonder](https://github.com/dadoonet/elasticsearch-beyonder/) 
instead. Just inject an Elasticsearch Rest Client bean in your code and use the
`ElasticsearchBeyonder` class to load your indices, templates... Something like:

```java
import org.springframework.context.annotation.Bean;

public class MyApp {
    @Bean
    ElasticsearchClient client;

    public void startBeyonder() {
        ElasticsearchBeyonder.start(client);
    }
}
```

---

Welcome to the Spring factories for [Elasticsearch](https://www.elastic.co/elasticsearch/) project.

The factory provides a [Java Rest Client for Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/)
and automatically create index settings and templates based on what is found in the classpath:

* `/es/_index_lifecycles/` for [index lifecycles policies](#index-lifecycles-policies)
* `/es/INDEXNAME/_settings.json` for [index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/INDEXNAME/_update_settings.json` to [update existing index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/_component_templates/` for [component templates](#component-templates)
* `/es/_index_templates/` for [index templates](#index-templates)
* `/es/_pipelines/` for [ingest pipelines](#ingest-pipelines)
* `/es/_aliases.json` for [aliases](#aliases)

## Documentation

* For 8.x elasticsearch versions, you are reading the latest documentation.
* For 7.x elasticsearch versions, look at [es-7.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-7.x).
* For 6.x elasticsearch versions, look at [es-6.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-6.x).
* For 5.x elasticsearch versions, look at [es-5.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-5.x).
* For 2.x elasticsearch versions, look at [es-2.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-2.x).
* For 1.x elasticsearch versions, look at [es-1.4 branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-1.4).
* For 0.x elasticsearch versions, look at [0.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/0.x).

| spring-elasticsearch | elasticsearch | Spring | Release date |
|:--------------------:|:-------------:|:------:|:------------:|
|     8.7-SNAPSHOT     |     8.x       | 6.0.7  |              |
|         7.1          |   7.0 - 7.x   | 5.3.15 |  2022-01-13  |
|         7.0          |   7.0 - 7.x   | 5.3.8  |  2021-06-21  |
|         6.7          |   6.7 - 6.x   | 5.1.3  |  2019-04-13  |
|         6.2          |   6.0 - 6.x   | 5.1.3  |  2019-01-08  |
|         6.1          |   6.0 - 6.x   | 5.0.7  |  2018-07-22  |
|         6.0          |   6.0 - 6.x   | 5.0.3  |  2018-02-08  |
|         5.0          |   5.0 - 5.x   | 4.3.10 |  2018-02-04  |
|        2.2.0         |   2.0 - 2.4   | 4.2.3  |  2017-03-09  |
|        2.1.0         |   2.0, 2.1    | 4.2.3  |  2015-11-25  |
|        2.0.0         |      2.0      | 4.1.4  |  2015-10-25  |
|        1.4.2         |     < 2.0     | 4.1.4  |  2015-03-03  |
|        1.4.1         |      1.4      | 4.1.4  |  2015-02-28  |
|        1.4.0         |      1.4      | 4.1.4  |  2015-01-03  |
|        1.3.0         |      1.3      | 4.0.6  |  2014-09-01  |
|        1.0.0         |      1.0      | 3.2.2  |  2014-02-14  |

## Build Status

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.pilato.spring/spring-elasticsearch/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/fr.pilato.spring/spring-elasticsearch/)
[![Build Status](https://github.com/dadoonet/spring-elasticsearch/actions/workflows/maven.yml/badge.svg)](https://github.com/dadoonet/spring-elasticsearch/actions/workflows/maven.yml)

## Release notes

### Changes in 8.7

* Update to Spring 6.0.7
* Update to Java 17 (needed by Spring 6)
* Update to Beyonder 8.6
* Provides now the new official [Java Rest Client for Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/)
* Removed deprecated templates
* Removed deprecated XML support
* As a side effect of a previous removal (TransportClient), async initialization of the client has been removed.
* Deprecated `setProperties(Properties)` method.
* Add a wait for yellow health when creating a new index

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
  <version>8.7</version>
</dependency>
```

If you want to set a specific version of the Elasticsearch Java client, add it to your `pom.xml` file:

```xml
<dependencies>
  <dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>${elasticsearch.version}</version>
  </dependency>
  <dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>${elasticsearch.version}</version>
  </dependency>
</dependencies>
```

If you want to try out the most recent SNAPSHOT version [deployed on Sonatype](https://oss.sonatype.org/content/repositories/snapshots/fr/pilato/spring/spring-elasticsearch/):

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>8.8-SNAPSHOT</version>
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
<dependencies>
  <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-api</artifactId>
      <version>2.20.0</version>
  </dependency>
  <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-slf4j2-impl</artifactId>
      <version>2.20.0</version>
  </dependency>
  <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>2.20.0</version>
  </dependency>
</dependencies>
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
            <version>8.7</version>
        </dependency>
    </dependencies>
</project>
```

`App.java`:

```java
package fr.pilato.tests;

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
    public ElasticsearchClient esClient() {
      ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
      factory.setEsNodes(new String[]{"https://127.0.0.1:9200"});
      factory.setUsername("elastic");
      factory.setPassword("changeme");
      factory.afterPropertiesSet();
      return factory.getObject();
    }
  }

  @Autowired
  private ElasticsearchClient client;

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.scan("fr.pilato.tests");
    context.refresh();

    RestApp p = context.getBean(RestApp.class);
    p.run();

    context.close();
  }

  private void run() {
    // Run an advanced request
    client.info();

    // You still have access to the Low Level client
    client.getLowLevel().performRequest(new Request("GET", "/"));
  }
}
```

## Features

The factory provides a [Java Rest Client for Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/)
and automatically create index settings and templates based on what is found in the classpath:

* `/es/INDEXNAME/_settings.json` for [index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/INDEXNAME/_update_settings.json` to [update existing index settings and mappings](#indices) for a given index `INDEXNAME`
* `/es/_component_templates/` for [component templates](#component-templates)
* `/es/_index_templates/` for [index templates](#index-templates)
* `/es/_pipelines/` for [ingest pipelines](#ingest-pipelines)
* `/es/_aliases.json` for [aliases](#aliases)
* `/es/_index_lifecycles/` for [index lifecycles policies](#index-lifecycles-policies)

### Autoscan

By default, the factory will scan the classpath inside the default `/es` directory.
You can disable the autoscan and then provide manually every name for indices, templates...

```java
ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
factory.setAutoscan(false);
factory.setIndices(new String[]{"twitter"});
```

### Default directory

You can change the default directory from `/es` to something else. The factory will look into this
directory to find the indices and the settings for the indices, templates...

```java
ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
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
ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
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

### SSL certificates

If you need to specify your own SSL certificates (self-signed certificates), you can use the `setSSLContext(SSLContext)`
method to do this.
You can refer to the Elasticsearch [Low Level client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/_encrypted_communication.html)
to see some examples. Once you have created a SSLContext, you can use it in the factory:

```java
Path trustStorePath = Paths.get("/path/to/truststore.p12");
KeyStore truststore = KeyStore.getInstance("pkcs12");
try (InputStream is = Files.newInputStream(trustStorePath)) {
    truststore.load(is, keyStorePass.toCharArray());
}
SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
final SSLContext sslContext = sslBuilder.build();

ElasticsearchClientFactoryBean factory = new ElasticsearchClientFactoryBean();
factory.setSslContext(sslContext);
```

# Thanks

Special thanks to

- [Nicolas Huray](https://github.com/nhuray) for his contribution about
[templates](https://github.com/dadoonet/spring-elasticsearch/pull/4)
- [Nicolas Labrot](https://github.com/nithril) for his contribution about
[async](https://github.com/dadoonet/spring-elasticsearch/pull/30)

# Running tests

If you want to run tests (integration tests) from your IDE, you need to start first an elasticsearch instance.
Tests are expecting a node running at `https://localhost:9200` with the user `elastic` and `changeme` as the password.

To run the tests using Maven (on the CLI), just run:

```sh
mvn clean install
```

You can change the target to run the tests. For example, if you want to run the tests against an elastic cloud instance:

```shell
mvn clean install -Dtests.cluster=https://ID.es.ZONE.PROVIDER.cloud.es.io -Dtests.cluster.user=myuser -Dtests.cluster.pass=YOURPASSWORD
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

	Copyright 2011-2023 David Pilato
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may not
	use this file except in compliance with the License. You may obtain a copy of
	the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	License for the specific language governing permissions and limitations under
	the License.
