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
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class ResourceInSpecificFolderContribution implements BusinessArchiveContribution {

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File folder = new File(barFolder, getFolderName());
        if (folder.exists() && !folder.isFile()) {
            final File[] listFiles = folder.listFiles();
            for (final File file : listFiles) {
                final byte[] content = IOUtil.getContent(file);
                businessArchive.addResource(getFolderName() + '/' + file.getName(), content);
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

        for (final Entry<String, byte[]> entry : resources.entrySet()) {
            final File file = new File(folder, entry.getKey().substring(beginIndex));
            IOUtil.write(file, entry.getValue());
        }
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

}
