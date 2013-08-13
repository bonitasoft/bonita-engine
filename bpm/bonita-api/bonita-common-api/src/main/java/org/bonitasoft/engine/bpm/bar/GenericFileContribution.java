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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class GenericFileContribution implements BusinessArchiveContribution {

    public abstract String getFileName();

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        // final Set<String> fileNames = businessArchive.getResources(getFileName()).keySet();
        // final File[] listFiles = connectorFolder.listFiles();
        final File file = new File(barFolder, getFileName());
        if (!file.exists()) {
            return false;
        }
        FileChannel ch = null;
        FileInputStream fin = null;
        // for (final File file : listFiles) {
        try {
            fin = new FileInputStream(file);
            ch = fin.getChannel();
            final int size = (int) ch.size();
            final MappedByteBuffer buf = ch.map(MapMode.READ_ONLY, 0, size);
            final byte[] bytes = new byte[size];
            buf.get(bytes);
            businessArchive.addResource(getFileName(), bytes);
        } finally {
            if (fin != null) {
                fin.close();
            }
            if (ch != null) {
                ch.close();
            }
        }
        ch = null;
        fin = null;

        return true;
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final Map<String, byte[]> resources = businessArchive.getResources(getFileName());
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        for (final Entry<String, byte[]> entry : resources.entrySet()) {
            final byte[] value = entry.getValue();
            if (value != null) {
                try {
                    fos = new FileOutputStream(new File(barFolder, entry.getKey()));
                    bos = new BufferedOutputStream(fos);
                    bos.write(value);
                } finally {
                    if (bos != null) {
                        bos.close();
                    } else {
                        if (fos != null) {
                            fos.close();
                        }
                    }
                }
            }
            fos = null;
            bos = null;
        }
    }

}
