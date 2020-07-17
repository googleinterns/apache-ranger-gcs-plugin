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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidParameterException;

@RunWith(JUnit4.class)
public class ConfigParserTest {
    private static final String HOST = "test-host";
    private static final String PORT = "12345";

    /**
     * A correct config that contains all the necessary fields.
     */
    @Test
    public void testCorrectParse() throws Exception{
        String fakeConfig =
                "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n" +
                "<configuration xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n" +
                "    <property>\n" +
                "        <name>permissionCheckService.ip</name>\n" +
                "        <value>"+ HOST +"</value>\n" +
                "    </property>\n" +
                "\n" +
                "    <property>\n" +
                "        <name>permissionCheckService.port</name>\n" +
                "        <value>" + PORT + "</value>\n" +
                "    </property>\n" +
                "</configuration>\n";

        InputStream is = new ByteArrayInputStream(fakeConfig.getBytes());
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(is);
        Config config = new ConfigParser().parseConfig(document);

        assertThat(config.getHostIp()).isEqualTo(HOST);
        assertThat(config.getHostPort()).isEqualTo(Integer.parseInt(PORT));
    }

    @Test
    public void testMissingPort() throws Exception{
        String fakeConfig =
                "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n" +
                "<configuration xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n" +
                "    <property>\n" +
                "        <name>permissionCheckService.ip</name>\n" +
                "        <value>"+ HOST +"</value>\n" +
                "    </property>\n" +
                "</configuration>\n";

        InputStream is = new ByteArrayInputStream(fakeConfig.getBytes());
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(is);
        assertThrows(InvalidParameterException.class, () -> {new ConfigParser().parseConfig(document);});
    }

    @Test
    public void testMissingHost() throws Exception{
        String fakeConfig =
                "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n" +
                "<configuration xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n" +
                "    <property>\n" +
                "        <name>permissionCheckService.port</name>\n" +
                "        <value>" + PORT + "</value>\n" +
                "    </property>\n" +
                "</configuration>\n";

        InputStream is = new ByteArrayInputStream(fakeConfig.getBytes());
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(is);
        assertThrows(InvalidParameterException.class, () -> {new ConfigParser().parseConfig(document);});
    }
}
