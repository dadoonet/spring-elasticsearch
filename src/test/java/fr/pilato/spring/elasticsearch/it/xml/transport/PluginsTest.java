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

package fr.pilato.spring.elasticsearch.it.xml.transport;

import org.elasticsearch.client.Client;
import org.elasticsearch.plugins.Plugin;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class PluginsTest extends AbstractXmlContextModel {
    private final String[] xmlBeans = {"models/transport/plugins/plugins-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

    @Override
    protected void checkUseCaseSpecific(Client client) {
        assertThat(PluginsTest.Dummy1Plugin.isPluginCalled(), is(true));
        assertThat(PluginsTest.Dummy2Plugin.isPluginCalled(), is(true));
    }

    public static class Dummy1Plugin extends Plugin {
        private static final AtomicBoolean pluginCalled = new AtomicBoolean(false);
        public Dummy1Plugin() {
            pluginCalled.set(true);
        }
        static boolean isPluginCalled() {
            return pluginCalled.get();
        }
    }

    public static class Dummy2Plugin extends Plugin {
        private static final AtomicBoolean pluginCalled = new AtomicBoolean(false);
        public Dummy2Plugin() {
            pluginCalled.set(true);
        }
        static boolean isPluginCalled() {
            return pluginCalled.get();
        }
    }

}
