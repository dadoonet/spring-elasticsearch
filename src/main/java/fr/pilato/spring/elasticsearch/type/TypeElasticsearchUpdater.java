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

package fr.pilato.spring.elasticsearch.type;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage elasticsearch types (mappings)
 * @author David Pilato
 */
public class TypeElasticsearchUpdater {

    private static final Logger logger = LoggerFactory.getLogger(TypeElasticsearchUpdater.class);

    /**
     * Create a new type in Elasticsearch
     * @param client Elasticsearch client
     * @param root dir within the classpath
     * @param index Index name
     * @param merge Try to merge mapping if type already exists
     * @throws Exception if the elasticsearch call is failing
     */
    public static void createMapping(RestHighLevelClient client, String root, String index, boolean merge)
            throws Exception {
        String mapping = TypeSettingsReader.readMapping(root, index);
        if (mapping != null) {
            createMappingWithJson(client, index, mapping, merge);
        }
    }

    /**
     * Create a new type in Elasticsearch
     * @param client Elasticsearch client
     * @param index Index name
     * @param mapping Mapping if any, null if no specific mapping
     * @param merge Try to merge mapping if type already exists
     * @throws Exception if the elasticsearch call is failing
     */
    private static void createMappingWithJson(RestHighLevelClient client, String index, String mapping, boolean merge)
            throws Exception {
        boolean mappingExist = isMappingExist(client, index);
        if (merge || !mappingExist) {
            if (mappingExist) {
                logger.debug("Updating type for index [{}].", index);
            } else {
                logger.debug("Mapping for [{}] index doesn't exist. Creating it.", index);
            }
            createTypeWithMappingInElasticsearch(client, index, mapping);
        } else {
            logger.debug("Mapping already exists in index [{}] and merge is not set.", index);
        }

        if (mappingExist) {
            logger.debug("Mapping definition for [{}] index succesfully merged.", index);
        } else {
            logger.debug("Mapping definition for [{}] index succesfully created.", index);
        }
    }

    /**
     * Check if an index already exist
     * @param client Elasticsearch client
     * @param index Index name
     * @return true if the index already exist
     * @throws Exception if the elasticsearch call is failing
     */
    private static boolean isMappingExist(RestHighLevelClient client, String index) throws Exception {
        GetMappingsResponse mapping = client.indices().getMapping(new GetMappingsRequest().indices(index), RequestOptions.DEFAULT);
        // Let's get the default mapping
        if (mapping.mappings().isEmpty()) {
            return false;
        }
        MappingMetadata doc = mapping.mappings().values().iterator().next();
        return !doc.getSourceAsMap().isEmpty();
    }

    /**
     * Create a new type in Elasticsearch
     * @param client Elasticsearch client
     * @param index Index name
     * @param mapping Mapping if any, null if no specific mapping
     * @throws Exception if the elasticsearch call is failing
     */
    private static void createTypeWithMappingInElasticsearch(RestHighLevelClient client, String index, String mapping)
            throws Exception {
        logger.trace("createTypeWithMappingInElasticsearch([{}])", index);

        assert client != null;
        assert index != null;

        if (mapping != null) {
            // Create mapping
            client.indices().putMapping(new PutMappingRequest(index).source(mapping, XContentType.JSON), RequestOptions.DEFAULT);
        } else {
            logger.trace("no content given for mapping. Ignoring mapping creation in index [{}].", index);
        }

        logger.trace("/createTypeWithMappingInElasticsearch([{}])", index);
    }
}
