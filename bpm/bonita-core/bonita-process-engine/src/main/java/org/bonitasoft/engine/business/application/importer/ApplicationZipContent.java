/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.importer;

import static org.bonitasoft.engine.commons.io.IOUtil.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import org.bonitasoft.engine.exception.ImportException;

@Data
class ApplicationZipContent {

    private final byte[] iconRaw;

    private final byte[] xmlRaw;

    private final String pngName;

    static ApplicationZipContent getApplicationZipContent(String resourceName, InputStream resourceAsStream)
            throws IOException, ImportException {
        final byte[] content = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
        Map<String, byte[]> zipContent = unzip(content);
        List<String> pngFileNamesList = zipContent.keySet().stream().filter(l -> l.endsWith(".png"))
                .collect(Collectors.toList());
        List<String> xmlFileNamesList = zipContent.keySet().stream().filter(l -> l.endsWith(".xml"))
                .collect(Collectors.toList());
        if (xmlFileNamesList.size() > 1) {
            throw new ImportException("The application zip " + resourceName
                    + " contains more than one xml descriptor, and therefore has an invalid format");
        } else if (pngFileNamesList.size() > 1) {
            throw new ImportException("The application zip " + resourceName
                    + " contains more than one icon file, and therefore has an invalid format");
        }
        String pngName = pngFileNamesList.get(0);
        String xmlName = xmlFileNamesList.get(0);
        return new ApplicationZipContent(zipContent.get(pngName), zipContent.get(xmlName), pngName);
    }
}
