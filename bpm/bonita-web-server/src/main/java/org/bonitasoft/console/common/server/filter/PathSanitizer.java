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
package org.bonitasoft.console.common.server.filter;

import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to sanitize URL paths by stripping path parameters (semicolons).
 * <p>
 * Per RFC 3986, semicolons in path segments introduce matrix parameters.
 * Some servlet containers (e.g. Tomcat) strip semicolons when resolving
 * {@code getRequestDispatcher()} paths, while {@code java.net.URI.normalize()}
 * treats them as literal characters. This differential can be exploited for
 * path traversal attacks (e.g. {@code /API/..;/..;/serverAPI}).
 * Using this class prevents such attacks by normalizing and sanitizing
 * URLs before processing them (fixes Bonita CVE-233).
 * </p>
 */
@Slf4j
public final class PathSanitizer {

    private static final Pattern SEMICOLON_PARAMS = Pattern.compile(";[^/]*");

    private PathSanitizer() {
    }

    /**
     * Strips path parameters (everything from ';' to the next '/' or end of string)
     * from each segment of the given path.
     * <p>
     * Example: {@code /API/..;/..;anything/serverAPI} becomes {@code /API/../../serverAPI}
     * </p>
     *
     * @param path the URL path to sanitize
     * @return the sanitized path with path parameters removed, or {@code null} if the input is {@code null}
     */
    public static String stripPathParameters(String path) {
        if (path == null || path.indexOf(';') < 0) {
            return path;
        }
        String sanitized = SEMICOLON_PARAMS.matcher(path).replaceAll("");
        log.debug("Path parameters stripped from URL path: [{}] -> [{}]", path, sanitized);
        return sanitized;
    }
}
