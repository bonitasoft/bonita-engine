/*
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
 */
package org.bonitasoft.engine.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * author Emmanuel Duchastenier
 */
public class CommonTestUtil {

    public static byte[] createTestPageContent(final String pageName, final String displayName, final String description)
            throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry("Index.groovy"));
        zos.write("return \"\";".getBytes());

        zos.putNextEntry(new ZipEntry("page.properties"));
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name=");
        stringBuilder.append(pageName);
        stringBuilder.append("\n");
        stringBuilder.append("displayName=");
        stringBuilder.append(displayName);
        stringBuilder.append("\n");
        stringBuilder.append("description=");
        stringBuilder.append(description);
        stringBuilder.append("\n");
        zos.write(stringBuilder.toString().getBytes());

        zos.closeEntry();
        return baos.toByteArray();
    }
}
