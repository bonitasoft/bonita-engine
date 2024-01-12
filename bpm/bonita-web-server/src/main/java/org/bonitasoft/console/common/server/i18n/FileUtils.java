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
package org.bonitasoft.console.common.server.i18n;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vincent Elcrin
 */
public class FileUtils {

    public static List<File> getMatchingFiles(final String regex, List<File> files) {
        List<File> locales = new ArrayList<>();
        for (File file : files) {
            if (isFileMatching(file, regex)) {
                locales.add(file);
            }
        }
        return locales;
    }

    private static boolean isFileMatching(File file, String regex) {
        return file.isFile() && file.getName().matches(regex);
    }

    public static List<File> listDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            files = new File[0];
        }
        return Arrays.asList(files);
    }

}
