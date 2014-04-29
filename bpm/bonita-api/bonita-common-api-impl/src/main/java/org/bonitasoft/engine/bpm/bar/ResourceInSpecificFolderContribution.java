/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
 * @author Baptiste Mesta
 */
public abstract class ResourceInSpecificFolderContribution implements BusinessArchiveContribution {

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File folder = new File(barFolder, getFolderName());
        if (folder.exists() && !folder.isFile()) {
            final File[] listFiles = folder.listFiles();
            FileChannel ch = null;
            FileInputStream fin = null;
            for (final File file : listFiles) {
                try {
                    fin = new FileInputStream(file);
                    ch = fin.getChannel();
                    final int size = (int) ch.size();
                    final MappedByteBuffer buf = ch.map(MapMode.READ_ONLY, 0, size);
                    final byte[] bytes = new byte[size];
                    buf.get(bytes);
                    businessArchive.addResource(getFolderName() + '/' + file.getName(), bytes);
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
            }
            return listFiles.length > 0;
        }
        return false;
    }

    protected abstract String getFolderName();

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File folder = new File(barFolder, getFolderName());
        folder.mkdir();
        final int beginIndex = getFolderName().length();
        final Map<String, byte[]> resources = businessArchive.getResources("^" + getFolderName() + "/.*$");
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        for (final Entry<String, byte[]> entry : resources.entrySet()) {
            try {
                fos = new FileOutputStream(new File(folder, entry.getKey().substring(beginIndex)));
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

}
