/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.home;

import java.io.File;

import org.bonitasoft.engine.commons.StringUtils;

/**
 * @author Charles Souillard
 */
public class Util {

    public static String generateRelativeResourcePath(final File folder, final File file) {
        String path = file.getAbsolutePath().replace(folder.getAbsolutePath(), "");
        path = StringUtils.uniformizePathPattern(path);
        // remove first slash, if any:
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
