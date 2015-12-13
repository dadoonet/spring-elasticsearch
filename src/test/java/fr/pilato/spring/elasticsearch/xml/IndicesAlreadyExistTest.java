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

import org.junit.Test;


public class IndicesAlreadyExistTest extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/indices-already-exist-86/indices-already-exist-86.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

	@Test
	public void test_simple_node() {
        // We don't test really something.
        // We just expect that there won't be any exception while starting the test
        // If someone wants to check anything, he has to read the logs for this test
        // and check that "we have to check some indices status as they already exist..."
        // appears...
	}
}
