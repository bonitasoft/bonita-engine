/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.bar;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;

/**
 * Checks the resource paths of a business archive. They become file paths when the business archive is written to a
 * folder (e.g. on process export), so a path that leaves the archive must not be accepted.
 */
public final class BusinessArchiveResourcePaths {

    public static final String RESOURCE_OUTSIDE_OF_ARCHIVE_MESSAGE = "The business archive contains a resource whose path is outside of the archive";

    private static final Pattern DRIVE_LETTER = Pattern.compile("^[a-zA-Z]:");

    private BusinessArchiveResourcePaths() {
    }

    /**
     * A resource path is outside of the archive when it is absolute, starts with a drive letter, or goes above the
     * root of the archive with '..' segments. '\' is handled as a separator whatever the OS, so that an archive is
     * checked the same way wherever it is written or extracted later. On Windows, a path containing a character that
     * is not allowed there (e.g. ':' or '*') is rejected too.
     *
     * @return true if one of the resources of the business archive has a path outside of the archive
     */
    public static boolean hasResourceOutsideOfArchive(BusinessArchive businessArchive) {
        return businessArchive.getResources().keySet().stream()
                .anyMatch(resourcePath -> !isInsideArchive(resourcePath));
    }

    private static boolean isInsideArchive(String resourcePath) {
        if (resourcePath == null) {
            return false;
        }
        final String portablePath = resourcePath.replace('\\', '/');
        if (portablePath.startsWith("/") || DRIVE_LETTER.matcher(portablePath).find()) {
            return false;
        }
        try {
            final Path path = Path.of(portablePath);
            return path.getRoot() == null && !path.normalize().startsWith("..");
        } catch (final InvalidPathException e) {
            return false;
        }
    }
}
