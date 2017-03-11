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

package fr.pilato.spring.elasticsearch.it.xml;

import org.elasticsearch.plugins.Plugin;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class PluginsTest extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/plugins/plugins-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

	@Test
	public void test_plugin() throws IOException {
		checkClient("esClient");
		assertThat(plugin1Called.get(), is(true));
		assertThat(plugin2Called.get(), is(true));
    }

    private static AtomicBoolean plugin1Called = new AtomicBoolean(false);
    private static AtomicBoolean plugin2Called = new AtomicBoolean(false);

    public static class Dummy1Plugin extends Plugin {
        public Dummy1Plugin() {
            plugin1Called.set(true);
        }
    }

    public static class Dummy2Plugin extends Plugin {
        public Dummy2Plugin() {
            plugin2Called.set(true);
        }
    }

}
