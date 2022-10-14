/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.bonitasoft.engine.io.IOUtil;

/**
 * Utility class extracting zip file
 *
 * @author Zhiheng Yang
 */
public class UnzipUtil {

    /**
     * Unzip a zip file from InputStream.
     * Client is responsible to close the input stream.
     */
    public static synchronized void unzip(final InputStream sourceFile, final String targetPath) throws IOException {
        IOUtil.unzipToFolder(sourceFile, new File(targetPath));
    }

    /**
     * Unzip a zip file from InputStream
     *
     * @param zipFile
     *        of SourceFile
     * @param targetPath
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static synchronized void unzip(final File zipFile, final String targetPath)
            throws FileNotFoundException, IOException {
        try (final FileInputStream zipFileInputStream = new FileInputStream(zipFile)) {
            unzip(zipFileInputStream, targetPath);
        }

    }

    public static synchronized void unzip(final File zipFile, final String targetPath, final boolean deleteFileAfterZip)
            throws IOException {
        unzip(zipFile, targetPath);
        if (deleteFileAfterZip) {
            zipFile.delete();
        }
    }

}
