/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.commons.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

public class IOUtilTest {

    @Rule
    public TemporaryFolder tempFolderRule = new TemporaryFolder();

    @Test
    public void getClassNameList() throws Exception {
        // given:
        final byte[] jarContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("bdr-jar.bak").getFile()));

        // when:
        final List<String> classNameList = IOUtil.getClassNameList(jarContent);

        // then:
        assertThat(classNameList).containsOnly("org.bonita.pojo.Employee");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldToByteArray_ThrowIlllegalArgumentException_ForNullDocument() throws Exception {
        final Document document = null;
        IOUtil.toByteArray(document);
    }

    @Test
    public void shouldToByteArray_ForDocumentReturnAByteArray() throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        InputStream is = null;
        Document document = null;
        try {
            is = IOUtilTest.class.getResourceAsStream("persistence.xml");
            document = documentBuilder.parse(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        final byte[] byteArray = IOUtil.toByteArray(document);
        assertThat(byteArray).isNotNull();
    }

    @Test
    public void shouldAddJarEntry_AddAnEntryInExistingJar() throws Exception {
        final byte[] jarContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("bdr-jar.bak").getFile()));
        final byte[] entryContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("persistence.xml").getFile()));
        final String entryName = "META-INF/myNewEntry.xml";
        final byte[] updatedJar = IOUtil.addJarEntry(jarContent, entryName, entryContent);
        assertThat(updatedJar).isNotNull();

        final ByteArrayInputStream bais = new ByteArrayInputStream(updatedJar);
        final JarInputStream jis = new JarInputStream(bais);
        JarEntry entry = null;
        final Map<String, byte[]> entryNames = new HashMap<String, byte[]>();
        final byte[] buffer = new byte[4096];
        while ((entry = jis.getNextJarEntry()) != null) {
            if (!entry.isDirectory()) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                while ((len = jis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                baos.close();
                entryNames.put(entry.getName(), baos.toByteArray());
            }
        }
        jis.close();
        assertThat(entryNames.keySet()).contains(entryName);
        assertThat(entryNames.get(entryName)).isEqualTo(entryContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddJarEntry_ThrowIllegalArgumentExceptionIfEntryAlreadyExists() throws Exception {
        final byte[] jarContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("bdr-jar.bak").getFile()));
        final byte[] entryContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("persistence.xml").getFile()));
        final String entryName = "META-INF/persistence.xml";
        IOUtil.addJarEntry(jarContent, entryName, entryContent);
    }

    @Test
    public void testUpdatePropertyValue() throws IOException {
        final Properties properties = new Properties();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        properties.put("key3", "value3");


        final File file = tempFolderRule.newFile("testPropertiesFile");
        PropertiesManager.saveProperties(properties, file);
        final String updatedValue2 = "@\\[||sfgf23465";
        final Map<String, String> pairs = new HashMap<>();
        pairs.put("key2", updatedValue2);
        IOUtil.updatePropertyValue(file, pairs);


        final Properties updatedProperties = PropertiesManager.getProperties(file);
        Assert.assertEquals(updatedValue2, updatedProperties.get("key2"));

    }
}
