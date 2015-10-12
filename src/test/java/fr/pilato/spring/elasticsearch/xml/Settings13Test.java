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

package fr.pilato.spring.elasticsearch.xml;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.TransportAddress;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;


public class Settings13Test extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/settings-13/settings-13-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

	@Test
	public void test_transport_client() {
		Client client = checkClient();
        assertThat(client, instanceOf(org.elasticsearch.client.transport.TransportClient.class));

        TransportClient tClient = (TransportClient) client;
        List<TransportAddress> adresses = tClient.transportAddresses();
        assertThat(adresses, not(emptyCollectionOf(TransportAddress.class)));
        assertThat(adresses.size(), is(2));
	}
}
