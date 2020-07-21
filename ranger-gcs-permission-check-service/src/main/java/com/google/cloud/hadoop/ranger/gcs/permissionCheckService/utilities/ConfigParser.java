/*
 *
 *  * Copyright 2020 Google Inc. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parse "ranger-gcs-permission-check-service.xml" and store the settings in a "Config" instance.
 */
public class ConfigParser {
    private static final String PROPERTY = "property";

    private static final String NAME = "name";
    private static final String VALUE = "value";

    private static final String ERR_FILE_NOT_FOUND = "Can't find %s in classpath.";

    private static final String defaultFile = "ranger-gcs-permission-check-service.xml";
    private final String configFile;

    public static final String KEY_IP = "permissionCheckService.ip";
    public static final String KEY_PORT = "permissionCheckService.port";

    public ConfigParser() {
        configFile = defaultFile;
    }

    /**
     * Read config file from classpath and return a parsed Config object.
     */
    public Config parseConfig() throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new File(getFile(configFile)));

        return parseConfig(document);
    }

    /**
     * Parse a document object containing a config file and return a parsed Config object.
     */
    public Config parseConfig(Document document) {
        Config.ConfigBuilder re = new Config.ConfigBuilder();

        NodeList configs = document.getElementsByTagName(PROPERTY);

        for (int i = 0; i < configs.getLength(); ++i) {
            Node node = configs.item(i);

            String name = ((Element) node).getElementsByTagName(NAME).item(0).getTextContent();
            String value = ((Element) node).getElementsByTagName(VALUE).item(0).getTextContent();

            switch (name) {
                case KEY_IP:
                    re.setHostIp(value);
                    break;
                case KEY_PORT:
                    re.setHostPort(Integer.parseInt(value));
                    break;
            }
        }

        return re.build();
    }

    /**
     * Get a File object from class path.
     */
    public static String getFile(String fileName) throws FileNotFoundException{
        URL url = ConfigParser.class.getClassLoader().getResource(fileName);
        if (url == null)
            url = ConfigParser.class.getClassLoader().getResource("/" + fileName);
        if (url == null) {
            throw new FileNotFoundException(String.format(ERR_FILE_NOT_FOUND, fileName));
        }
        return url.getFile();
    }
}
