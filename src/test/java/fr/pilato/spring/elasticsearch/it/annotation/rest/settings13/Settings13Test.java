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

package fr.pilato.spring.elasticsearch.it.annotation.rest.settings13;

import fr.pilato.spring.elasticsearch.it.annotation.rest.AbstractRestAnnotationContextModel;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class Settings13Test extends AbstractRestAnnotationContextModel {

    @Override
    protected List<String> otherTestIndices() {
        return Collections.singletonList("rss");
    }

    protected void checkUseCaseSpecific(RestHighLevelClient client) throws IOException {
        assertShardsAndReplicas(client.getLowLevelClient(), "rss", 5, 1);
    }
}
