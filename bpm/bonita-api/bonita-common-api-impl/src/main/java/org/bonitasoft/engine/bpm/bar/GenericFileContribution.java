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
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public abstract class GenericFileContribution implements BusinessArchiveContribution {

    public abstract String getFileName();

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File file = new File(barFolder, getFileName());
        if (!file.exists()) {
            return false;
        }

        final byte[] content = IOUtil.getContent(file);
        businessArchive.addResource(getFileName(), content);
        return true;
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final Map<String, byte[]> resources = businessArchive.getResources(getFileName());

        for (final Entry<String, byte[]> entry : resources.entrySet()) {
            final byte[] value = entry.getValue();
            if (value != null) {
                final File file = new File(barFolder, entry.getKey());
                IOUtil.write(file, entry.getValue());
            }
        }
    }

}
