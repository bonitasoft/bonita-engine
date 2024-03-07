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
package org.bonitasoft.web.rest.server.framework.utils;

import java.io.File;

/**
 * @author Séverin Moussel
 */
public class FilePathBuilder {

    private final StringBuilder path = new StringBuilder();

    public FilePathBuilder(final String path) {
        super();
        insert(path);
    }

    public FilePathBuilder append(final String path) {
        // If null or empty, do nothing
        if (path == null || path.isEmpty()) {
            return this;
        }

        this.path.append(File.separator);

        insert(path);

        return this;
    }

    /**
     * @param path
     */
    private void insert(final String path) {
        if (path.endsWith(File.separator)) {
            this.path.append(path, 0, path.length() - 1);
        } else {
            this.path.append(path);
        }
    }

    @Override
    public String toString() {
        return this.path.toString();
    }

}
