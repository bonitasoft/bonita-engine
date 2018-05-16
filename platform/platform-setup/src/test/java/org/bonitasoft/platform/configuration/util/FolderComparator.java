/*
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.platform.configuration.util;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.comparator.ExtensionFileComparator;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * @author Laurent Leseigneur
 */
public class FolderComparator {

    private static final int ARE_EQUALS = 0;
    private static final String XML = "xml";
    private static final String PROPERTIES = "properties";

    public void compare(File configFolder, File destinationFolder) throws Exception {
        final Map<String, File> expectedFiles = flattenFolderFiles(configFolder);
        final Map<String, File> files = flattenFolderFiles(destinationFolder);
        assertThat(files).as("should have same size").hasSize(expectedFiles.size());
        assertThat(expectedFiles.keySet()).as("should have same file names").isEqualTo(files.keySet());
        for (String name : expectedFiles.keySet()) {
            compareFileContent(expectedFiles.get(name), files.get(name));
        }

    }

    private Map<String, File> flattenFolderFiles(File folder) throws IOException {
        Map<String, File> fileMap = new HashMap<>();
        final FlattenFolderVisitor flattenFolderVisitor = new FlattenFolderVisitor(fileMap);
        Files.walkFileTree(folder.toPath(), flattenFolderVisitor);
        return fileMap;
    }

    private void compareFileContent(File expectedFile, File givenFile) throws Exception {
        final String givenFileAbsolutePath = givenFile.getAbsolutePath();

        final String expectedFileExtension = getExtension(expectedFile.getName());
        final String expectedFileAbsolutePath = expectedFile.getAbsolutePath();
        assertThat(new ExtensionFileComparator().compare(expectedFile, givenFile))
                .as(expectedFileAbsolutePath + " and " + givenFileAbsolutePath + " should have same extension").isEqualTo(ARE_EQUALS);

        switch (expectedFileExtension) {
            case PROPERTIES:
                assertThat(getProperties(expectedFile))
                        .as(expectedFileAbsolutePath + " and " + givenFileAbsolutePath + " should contain same properties")
                        .isEqualTo(getProperties(givenFile));
                break;
            case XML:
                final List allDifferences = new DetailedDiff(
                        XMLUnit.compareXML(new FileReader(givenFile), new FileReader(expectedFile)))
                        .getAllDifferences();
                assertThat(allDifferences).as("should xml file be equals").isEmpty();
                break;
            default:
                fail("unexpected file:" + expectedFile.getAbsolutePath());
                break;
        }

    }

    private Properties getProperties(File propertyFile) throws IOException {
        final Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(propertyFile)) {
            properties.load(fileInputStream);
            return properties;
        }

    }

}
