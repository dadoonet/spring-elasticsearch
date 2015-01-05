/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package fr.pilato.spring.elasticsearch;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class BaseTest {

    protected ESLogger logger = ESLoggerFactory.getLogger(this.getClass().getName());
    private static File testDir;

    private static void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }

    @Before
    public void createTempDir() throws IOException {
        testDir = File.createTempFile("junit", "");
        testDir.delete();
        testDir.mkdir();
        File dataDir = new File(testDir, "data");
        File logsDir = new File(testDir, "logs");

        System.setProperty("es.path.data", dataDir.getAbsolutePath());
        System.setProperty("es.path.logs", logsDir.getAbsolutePath());
    }


    @After
    public void removeTempDir() {
        recursiveDelete(testDir);
        System.clearProperty("es.path.data");
        System.clearProperty("es.path.logs");
    }
}
