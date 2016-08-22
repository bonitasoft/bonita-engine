/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.platform.version;

import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Laurent Leseigneur
 */
public interface VersionService {

    /**
     * Retrieves the platform version in database
     *
     * @return platform current version
     */
    String getPlatformVersion() throws PlatformException;

    /**
     * Retrieves the platform setup tool version
     *
     * @return platform setup tool current version
     */
    String getPlatformSetupVersion();

    /**
     * Check if platform an platform setup tool are in same version
     *
     * @return true if same version, false otherwise
     */
    boolean isValidPlatformVersion() throws PlatformException;
}
