/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.api.impl.application.installer.detector;

import static org.bonitasoft.engine.io.FileOperations.getFileFromZip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.bonitasoft.engine.io.FileAndContent;

public class CustomPageDetector {

    private static final String CONTENT_TYPE_PROPERTY = "contentType";
    private static final String PAGE_PROPERTIES_FILE = "page.properties";

    public boolean isCompliant(FileAndContent file, String... contentTypes) {
        return getPageProperties(file)
                .map(properties -> properties.getProperty(CONTENT_TYPE_PROPERTY))
                .filter(Objects::nonNull)
                .filter(type -> Arrays.asList(contentTypes).contains(type))
                .isPresent();
    }

    private Optional<Properties> getPageProperties(FileAndContent file) {
        try {
            byte[] fileFromZip = getFileFromZip(file.getContent(), PAGE_PROPERTIES_FILE);
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(fileFromZip));
            return Optional.of(properties);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public boolean isFilePresent(FileAndContent file, String filename) {
        try {
            getFileFromZip(file.getContent(), filename);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
