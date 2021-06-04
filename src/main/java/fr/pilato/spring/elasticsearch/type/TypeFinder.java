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

import fr.pilato.elasticsearch.tools.util.ResourceList;
import fr.pilato.elasticsearch.tools.util.SettingsFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TypeFinder extends SettingsFinder {
    private static final Logger logger = LoggerFactory.getLogger(TypeFinder.class);

    /**
     * Find all types within an index in default classpath dir
     * @param index index name
     * @return The list of types
     * @throws IOException if the connection with elasticsearch is failing
     * @throws URISyntaxException this should not happen
     */
    public static List<String> findTypes(String index) throws IOException, URISyntaxException {
        return findTypes(Defaults.ConfigDir, index);
    }

    /**
     * Find all types within an index
     * @param root dir within the classpath
     * @param index index name
     * @return The list of types
     * @throws IOException if the connection with elasticsearch is failing
     * @throws URISyntaxException this should not happen
     */
    public static List<String> findTypes(String root, String index) throws IOException, URISyntaxException {
        if (root == null) {
            return findTypes(index);
        }

        logger.debug("Looking for types in classpath under [{}/{}].", root, index);

        final List<String> typeNames = new ArrayList<>();
        String[] resources = ResourceList.getResources(root + "/" + index + "/"); // "es/index/"
        for (String resource : resources) {
            if (!resource.isEmpty() && !root.equals(resource)) {
                String withoutIndex = resource.substring(resource.indexOf("/") + 1);
                if (withoutIndex.equals(Defaults.IndexSettingsFileName) ||
                        withoutIndex.equals(Defaults.UpdateIndexSettingsFileName)) {
                    logger.trace(" - ignoring: [{}]", withoutIndex);
                } else {
                    String type = withoutIndex.substring(0, withoutIndex.indexOf(Defaults.JsonFileExtension));
                    logger.trace(" - found [{}].", type);
                    typeNames.add(type);
                }
            }
        }

        return typeNames;
    }
}
