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

package fr.pilato.spring.elasticsearch.it.annotation;

import fr.pilato.spring.elasticsearch.it.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class AbstractAnnotationContextModel extends BaseTest {
    protected AnnotationConfigApplicationContext ctx;

    /**
     * @return list of specific classpath to load if not the default one
     */
    String classpath() {
        return null;
    }

    @BeforeEach
    void startContext() {
        String classpath = classpath();

        if (classpath == null || classpath.isEmpty()) {
            // If not set, the test package name is used.
            classpath = this.getClass().getPackage().getName();
        }

        logger.info("  --> Starting Spring Context on [{}] classpath", classpath);
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(SecurityOptionalConfig.class);
        ctx.scan(classpath);
        ctx.refresh();
    }

    @AfterEach
    void stopContext() {
        if (ctx != null) {
            logger.info("  --> Closing Spring Context");
            ctx.close();
        }
    }

    /**
     * Overwrite it if the number of expected shards is not 5
     * @return Number of expected primaries
     */
    protected int expectedShards() {
        return 1;
    }

    /**
     * Overwrite it if the number of expected replicas is not 1
     * @return Number of expected replicas
     */
    protected int expectedReplicas() {
        return 1;
    }

    protected String beanName() {
        return "esClient";
    }
}
