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

package fr.pilato.spring.elasticsearch.type;

import fr.pilato.elasticsearch.tools.SettingsFinder.Defaults;
import fr.pilato.elasticsearch.tools.SettingsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Manage elasticsearch type (mapping) files
 * @author David Pilato
 */
class TypeSettingsReader extends SettingsReader {

	private static final Logger logger = LoggerFactory.getLogger(TypeSettingsReader.class);

	/**
	 * Read a mapping from index/_doc.json file
	 * @param root dir within the classpath
	 * @param index index name
	 * @return The mapping
	 * @throws IOException if the connection with elasticsearch is failing
	 */
	static String readMapping(String root, String index) throws IOException {
		if (root == null) {
			return readMapping(index);
		}
		String mappingFile = root + "/" + index + "/_doc" + Defaults.JsonFileExtension;
		return readFileFromClasspath(mappingFile);
	}

	/**
	 * Read a mapping from index/_doc.json file in default classpath dir
	 * @param index index name
	 * @return The mapping
	 * @throws IOException if the connection with elasticsearch is failing
	 */
	private static String readMapping(String index) throws IOException {
		return readMapping(Defaults.ConfigDir, index);
	}
}
