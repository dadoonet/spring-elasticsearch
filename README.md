# Spring factories for Elasticsearch

Welcome to the Spring factories for [Elasticsearch](http://www.elasticsearch.org/) project.

Actually, since version 1.4.1, this project has been split in two parts:

* [Elasticsearch Beyonder](https://github.com/dadoonet/elasticsearch-beyonder/) which find resources in
project classpath to automatically create indices, types and templates.
* This project which is building Client beans using [Spring framework](http://projects.spring.io/spring-framework/).

From 5.0, this project provides 2 implementations of an elasticsearch Client:

* The REST client
* The Transport client (deprecated)

From 6.0, the REST client implementation has been replaced by a High Level REST client.
It now also supports [X-Pack](https://www.elastic.co/fr/products/x-pack) for official security.

Starting from 7.0, only `_doc` as a document type is supported if you are not providing the mapping within index settings.

## Documentation

* For 7.x elasticsearch versions, you are reading the latest documentation.
* For 6.x elasticsearch versions, look at [es-6.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-6.x).
* For 5.x elasticsearch versions, look at [es-5.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-5.x).
* For 2.x elasticsearch versions, look at [es-2.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-2.x).
* For 1.x elasticsearch versions, look at [es-1.4 branch](https://github.com/dadoonet/spring-elasticsearch/tree/es-1.4).
* For 0.x elasticsearch versions, look at [0.x branch](https://github.com/dadoonet/spring-elasticsearch/tree/0.x).

|   spring-elasticsearch  | elasticsearch |   Spring     | Release date |
|:-----------------------:|:-------------:|:------------:|:------------:|
|       7.0-SNAPSHOT      |  7.0 - 7.x    |    5.1.6     |              |
|            6.2          |  6.0 - 6.x    |    5.1.3     |  2019-01-08  |
|            6.1          |  6.0 - 6.x    |    5.0.7     |  2018-07-22  |
|            6.0          |  6.0 - 6.x    |    5.0.3     |  2018-02-08  |
|            5.0          |  5.0 - 5.x    |    4.3.10    |  2018-02-04  |
|           2.2.0         |  2.0 - 2.4    |    4.2.3     |  2017-03-09  |
|           2.1.0         |  2.0, 2.1     |    4.2.3     |  2015-11-25  |
|           2.0.0         |      2.0      |    4.1.4     |  2015-10-25  |
|           1.4.2         |     < 2.0     |    4.1.4     |  2015-03-03  |
|           1.4.1         |      1.4      |    4.1.4     |  2015-02-28  |
|           1.4.0         |      1.4      |    4.1.4     |  2015-01-03  |
|           1.3.0         |      1.3      |    4.0.6     |  2014-09-01  |
|           1.0.0         |      1.0      |    3.2.2     |  2014-02-14  |

## Build Status

Thanks to Travis for the [build status](https://travis-ci.org/dadoonet/spring-elasticsearch): 
[![Build Status](https://travis-ci.org/dadoonet/spring-elasticsearch.svg)](https://travis-ci.org/dadoonet/spring-elasticsearch)


## Getting Started

### Maven dependency

Import spring-elasticsearch in you project `pom.xml` file:

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>6.2</version>
</dependency>
```

If you want to set a specific version of the High Level Rest client, add it to your `pom.xml` file:

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>6.7.1</version>
</dependency>
```

If you want to use a transport client (deprecated), you must add it to your `pom.xml` file:

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>transport</artifactId>
    <version>6.7.1</version>
</dependency>
```

If you want to use a transport client secured with X-Pack (deprecated), you must add it to your `pom.xml` file:

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>x-pack-transport</artifactId>
    <version>6.7.1</version>
</dependency>
```

Note that you'd probably to add also the elastic maven repository:

```xml
<repositories>
    <repository>
        <id>elastic-download-service</id>
        <name>Elastic Download Service</name>
        <url>https://artifacts.elastic.co/maven/</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>false</enabled></snapshots>
    </repository>
</repositories>
```

If you want to try out the most recent SNAPSHOT version [deployed on Sonatype](https://oss.sonatype.org/content/repositories/snapshots/fr/pilato/spring/spring-elasticsearch/):

```xml
<dependency>
  <groupId>fr.pilato.spring</groupId>
  <artifactId>spring-elasticsearch</artifactId>
  <version>7.0-SNAPSHOT</version>
</dependency>
```

Don't forget to add if needed the following repository in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>oss-snapshots</id>
        <name>Sonatype OSS Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
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
    <version>2.11.2</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>2.11.2</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.11.2</version>
</dependency>
```

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

### Getting a rest client bean

You can get a REST High Level Client implementation.

#### Define a rest client bean

In your spring context file, just define a client like this:

```xml
<elasticsearch:rest-client id="esClient" />
```

By default, you will get an [Elasticsearch High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html)
connected to an Elasticsearch node already running at `http://localhost:9200`.

You can set the nodes you want to connect to:

```xml
<elasticsearch:rest-client id="esClient" esNodes="http://localhost:9200,http://localhost:9201" />
```

#### Injecting the rest client in your java project

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

#### Connecting to a secured X-Pack cluster

You need to define the `xpack.security.user` property as follows:

```
<util:properties id="esProperties">
    <prop key="xpack.security.user">elastic:changeme</prop>
</util:properties>

<elasticsearch:rest-client id="esClient" properties="esProperties" />
```

### Getting a transport client bean (deprecated)

From 5.0, the Transport Client implementation is deprecated. It is now marked as `optional` so
you need to explicitly add it to your project as described in [Maven dependency chapter](#maven-dependency).

#### Define a client Transport bean

In your spring context file, just define a client like this:

```xml
<elasticsearch:client id="esClient" />
```
    
By default, you will get an [Elasticsearch Transport Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html)
connected to an Elasticsearch node already running at `localhost:9300` using `elasticsearch` as cluster name.

You can set the nodes you want to connect to:

```xml
<elasticsearch:client id="esClient" esNodes="localhost:9300,localhost:9301" />
```

**Important notes**

> Note that you should define the same clustername as the one you defined on your running nodes.
> Otherwise, your Transport Client won't connect to the node. See [Elasticsearch properties](#transport-client-properties-deprecated).

> Note also that you must define the transport client port (9300-9399) and not the REST port (9200-9299).
> Transport client does not use REST API.

#### Injecting transport client in your java project

Now, you can use the transport client in your java classes.

```java
import org.elasticsearch.client.Client;

Client client = ctx.getBean("esClient", Client.class);
```

Better, you should use `@Autowired` annotation.

```java
// Inject your client...
@Autowired Client client;
```

#### Transport Client properties (deprecated)

You can define your transport client properties using a property file such as:

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

Injecting properties in client is now easy:

```xml
<elasticsearch:client id="esClient" properties="esproperties" />
```

You can also add plugins to the transport client in case it needs it:

```xml
<elasticsearch:client id="esClient" plugins="org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin" />
```

#### Connecting to a secured X-Pack cluster

You need to define the `xpack.security.user` property as follows:

```
<util:properties id="esProperties">
    <prop key="xpack.security.user">elastic:changeme</prop>
</util:properties>

<elasticsearch:client id="esClient" properties="esProperties" />
```

Note that it needs that you imported to your project the `x-pack-transport` jar.

#### Asynchronous initialization

Client bean initialization is by default synchronously. It can be initialized asynchronously with the attributes `async` and `taskExecutor`.

```xml
<task:executor pool-size="4" id="taskExecutor"/>
<elasticsearch:client id="esClient" async="true" taskExecutor="taskExecutor"/>
```
Asynchronous initialization does not block Spring startup but it continues on background on another thread.
Any methods call to these beans before elasticsearch is initialized will be blocked. `taskExecutor` references a standard Spring's task executor.

## Automatically create indices

The following examples are documented using the Rest Client implementation `elasticsearch:rest-client` but you can
replace them with the Transport client by using `elasticsearch:client` instead.

### Managing indexes and types

If you want to manage indices at startup (creating missing indices and applying optional mapping):

```xml
<elasticsearch:rest-client id="esClient"
    mappings="twitter" />
```

This will create an [Elasticsearch High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html)
and will create an index `twitter`.

If you need to manage more than one index, just use a comma separated list:

```xml
<elasticsearch:rest-client id="esClient"
    mappings="twitter,facebook" />
```

If you add in your classpath a file named `es/twitter/_settings.json`, it will be automatically applied to define
settings for your `twitter` index.

For example, create the following file `src/main/resources/es/twitter/_settings.json` in your project:

```javascript
{
  "settings" : {
    "number_of_shards" : 3,
    "number_of_replicas" : 2
  }
}
```

Also, if you define a file named `es/twitter/_doc.json`, it will be automatically applied as the mapping for
the `_doc` type in the `twitter` index.

For example, create the following file `src/main/resources/es/twitter/_doc.json` in your project:

```javascript
{
    "properties" : {
      "message" : {"type" : "text", "store" : "yes"}
    }
}
```

But in general, it's better to use one single `_settings.json` file which combines all that, like:

```javascript
{
  "settings" : {
    "number_of_shards" : 3,
    "number_of_replicas" : 2
  },
  "mappings" : {
    "properties" : {
      "message" : {"type" : "text", "store" : "yes"}
    }
  }
}
```


### Using convention over configuration

By default, the factory will find every mapping file located under `es` directory.
So, if you have a mapping file named `es/twitter/_doc.json` in your classpath, it will be automatically used by
the factory without defining anything:

```xml
<elasticsearch:rest-client id="esClient" />
```

You can disable this automatic lookup by setting the `autoscan` property to `false`:

```xml
<elasticsearch:rest-client id="esClient" autoscan="false" mappings="twitter" />
```

### Creating aliases to indexes

When creating an index, it could be useful to add an alias on it.
For example, if you planned to have indexes per year for twitter feeds (twitter2012, twitter2013, twitter2014) and you want
to define an alias named twitter, you can use the `aliases` property:

```xml
<elasticsearch:rest-client id="esClient"
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
<elasticsearch:rest-client id="esClient"
    templates="twitter_template,facebook_template" />
```

To configure your template you have to define a file named `es/_template/twitter_template.json` in your project:

```javascript
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

### Changing classpath search path for mapping and settings files

By default, the factory look in `es` classpath folder to find if there is index settings (`_settings.json`)
or mapping definition (`_doc.json`).
If you need to change it, you can use the `classpathRoot` property:

```xml
<elasticsearch:rest-client id="esClient" classpathRoot="myownfolder" />
```

So, if a `myownfolder/twitter/_settings.json` file exists in your classpath, it will be used by the factory.

### Merge mappings

If you need to merge mapping for an existing `type`, set  `mergeMapping` property to `true`.

```xml
<elasticsearch:rest-client id="esClient" mergeMapping="true" />
```

If merging fails, the factory will not start ([BeanCreationException](https://github.com/SpringSource/spring-framework/blob/master/spring-beans/src/main/java/org/springframework/beans/factory/BeanCreationException.java)
 will be raised).

### Merge settings

If you need to merge settings for an existing `index`, add a file named  `es/twitter/_update_settings.json` in your
classpath. The factory will detect it and will try to merge settings unless you explicitly set `mergeSettings` to `false`.

```xml
<elasticsearch:rest-client id="esClient" mergeSettings="false" />
```

If merging fails, the factory will not start.


### Force rebuild indices (use with caution)

For test purpose or for continuous integration, you could force the factory to clean the previous `indices` when starting the client.
It will *remove all your datas* for every index which has been defined. Just set  `forceMapping` property to `true`.

```xml
<elasticsearch:rest-client id="esClient" forceMapping="true" />
```

### Force rebuild templates (use with caution)

For test purpose or for continuous integration, you could force the factory to clean the previous `template` when starting the client.
Just set  `forceTemplate` property to `true`.

```xml
<elasticsearch:rest-client id="esClient" forceTemplate="true" />
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
            <version>6.2</version>
        </dependency>
    </dependencies>
</project>
```

`App.java`:

```java
package fr.pilato.tests;

import fr.pilato.spring.elasticsearch.ElasticsearchRestClientFactoryBean;
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
    private RestClient client;

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("fr.pilato.tests");
        context.refresh();

        RestApp p = context.getBean(RestApp.class);
        p.run();

        context.close();
    }

    private void run() {
        // Run a High Level request
        client.info();
        // You still have access to the Low Level client
        client.getLowLevel().performRequest("GET", "/");
    }
}
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
        <property name="mappings">
            <list>
                <value>twitter</value>
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

    <!-- The deprecated Transport Client -->
    <bean id="esTransportClient" class="fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean" >
        <property name="esNodes">
            <list>
                <value>localhost:9300</value>
                <value>localhost:9301</value>
            </list>
        </property>

        <property name="properties" ref="esproperties" />

        <property name="autoscan" value="false" />
        <property name="mappings">
            <list>
                <value>twitter</value>
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

# Thanks

Special thanks to

- [Nicolas Huray](https://github.com/nhuray) for his contribution about
[templates](https://github.com/dadoonet/spring-elasticsearch/pull/4)
- [Nicolas Labrot](https://github.com/nithril) for his contribution about
[async](https://github.com/dadoonet/spring-elasticsearch/pull/30)

# Running tests

If you want to run tests (integration tests) from your IDE, you need to start first an elasticsearch instance.

If you are not using x-pack, then just run the tests from your IDE. Tests are expecting a node running at `localhost:9200`.

If you are using x-pack, tests are expecting a user named `elastic` with `changeme` as the password.
You can set this user by running `bin/x-pack/setup-passwords interactive`.

To run the tests using Maven (on the CLI), just run:

```sh
mvn clean install
```

Note that when the tests are launched with maven, they are not running with x-pack yet.
To run tests against x-pack, you need to start elasticsearch with x-pack manually and run the tests with:

```sh
mvn clean install -Px-pack
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

	Copyright 2011-2019 David Pilato
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may not
	use this file except in compliance with the License. You may obtain a copy of
	the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	License for the specific language governing permissions and limitations under
	the License.
