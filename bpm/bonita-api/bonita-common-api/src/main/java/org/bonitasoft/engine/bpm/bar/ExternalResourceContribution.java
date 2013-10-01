/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Deals with the external resources in a BusinessArchive. Is considered external a resource that is not managed by the Bonita Engine.
 * 
 * @author Emmanuel Duchastenier
 */
public class ExternalResourceContribution implements BusinessArchiveContribution {

    public static final String EXTERNAL_RESOURCE_FOLDER = "resources";

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File externalResourceFolder = new File(barFolder, EXTERNAL_RESOURCE_FOLDER);
        if (externalResourceFolder.exists() && !externalResourceFolder.isFile()) {
            return readFromFileOrFolder(businessArchive, externalResourceFolder, null) > 0;
        } else {
            return false;
        }
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
            FileChannel ch = null;
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(fileOrFolder);
                ch = fin.getChannel();
                final int size = (int) ch.size();
                final MappedByteBuffer buf = ch.map(MapMode.READ_ONLY, 0, size);
                final byte[] bytes = new byte[size];
                buf.get(bytes);
                businessArchive.addResource(parentFolder + "/" + fileOrFolder.getName(), bytes);
                ch = null;
                return 1;
            } finally {
                if (fin != null) {
                    fin.close();
                }
                if (ch != null) {
                    ch.close();
                }
            }
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
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        for (final Entry<String, byte[]> entry : resources.entrySet()) {
            try {
                final File fullPathFile = new File(externalResourceFolder, entry.getKey().substring(beginIndex));
                fullPathFile.getParentFile().mkdirs();
                fos = new FileOutputStream(fullPathFile);
                bos = new BufferedOutputStream(fos);
                bos.write(entry.getValue());
            } finally {
                if (bos != null) {
                    bos.close();
                } else {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
            fos = null;
            bos = null;
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
