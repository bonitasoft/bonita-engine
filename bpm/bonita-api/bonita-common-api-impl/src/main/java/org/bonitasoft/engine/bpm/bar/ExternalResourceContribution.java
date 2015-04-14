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
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.io.IOUtil;

/**
 * Deals with the external resources in a BusinessArchive. Is considered external a resource that is not managed by the Bonita Engine.
 *
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class ExternalResourceContribution implements BusinessArchiveContribution {

    public static final String EXTERNAL_RESOURCE_FOLDER = "resources";

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File externalResourceFolder = new File(barFolder, EXTERNAL_RESOURCE_FOLDER);
        if (externalResourceFolder.exists() && !externalResourceFolder.isFile()) {
            final BarResourceVisitor barResourceVisitor = new BarResourceVisitor(businessArchive, barFolder.toPath());
            Files.walkFileTree(barFolder.toPath(), barResourceVisitor);
            return barResourceVisitor.getResourcesCount() > 0;

        }
        return false;
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File externalResourceFolder = new File(barFolder, EXTERNAL_RESOURCE_FOLDER);
        externalResourceFolder.mkdir();
        final int beginIndex = EXTERNAL_RESOURCE_FOLDER.length();
        final Map<String, byte[]> resources = businessArchive.getResources("^" + EXTERNAL_RESOURCE_FOLDER + "/.*$");

        for (final Entry<String, byte[]> entry : resources.entrySet()) {
            final File fullPathFile = new File(externalResourceFolder, entry.getKey().substring(beginIndex));
            fullPathFile.getParentFile().mkdirs();
            IOUtil.write(fullPathFile, entry.getValue());
        }
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public String getName() {
        return "resources";
    }

}
