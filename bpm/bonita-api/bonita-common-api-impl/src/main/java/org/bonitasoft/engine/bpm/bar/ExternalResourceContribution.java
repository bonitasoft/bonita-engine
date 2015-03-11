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
import java.io.FileNotFoundException;
import java.io.IOException;
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
            return readFromFileOrFolder(businessArchive, externalResourceFolder, null) > 0;
        }
        return false;
    }

    /**
     * @param businessArchive
     * @param fileOrFolder
     * @return the number of files recursively read if fileOrFolder is a directory, or 1 if fileOrFolder is a file and can be read.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private int readFromFileOrFolder(final BusinessArchive businessArchive, final File fileOrFolder, final String parentFolder) throws FileNotFoundException,
            IOException {
        if (fileOrFolder.isFile()) {
            final byte[] content = IOUtil.getContent(fileOrFolder);
            businessArchive.addResource(parentFolder + "/" + fileOrFolder.getName(), content);
            return 1;
        } else if (fileOrFolder.isDirectory()) {
            int nb = 0;
            for (final File file : fileOrFolder.listFiles()) {
                String parentFolder2 = fileOrFolder.getName();
                if (parentFolder != null) {
                    parentFolder2 = parentFolder + "/" + fileOrFolder.getName();
                }
                nb = nb + readFromFileOrFolder(businessArchive, file, parentFolder2);
            }
            return nb;
        } else {
            return 0;
        }
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
