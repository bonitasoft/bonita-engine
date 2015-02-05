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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class CheckPlatformVersion implements Callable<Boolean> {

    private final PlatformService platformService;

    private SPlatform platform;

    private SPlatformProperties platformProperties;

    private final BonitaHomeServer bonitaHomeServer;

    private String errorMessage;

    public CheckPlatformVersion(final PlatformService platformService, final BonitaHomeServer bonitaHomeServer) {
        this.platformService = platformService;
        this.bonitaHomeServer = bonitaHomeServer;
    }

    @Override
    public Boolean call() throws SBonitaException {
        // the database version
        platform = platformService.getPlatform();
        final String dbVersion = platform.getVersion();
        // the version in jars
        platformProperties = platformService.getSPlatformProperties();
        final String jarVersion = platformProperties.getPlatformVersion();
        // the version in bonita home
        final String bonitaHomeVersion = bonitaHomeServer.getVersion();
        final String platformMinorVersion = format(dbVersion);
        final String propertiesMinorVersion = format(jarVersion);
        boolean same = platformMinorVersion.equals(propertiesMinorVersion);
        if (!same) {
            errorMessage = "The version of the platform in database is not the same as expected: bonita-server version is <" + jarVersion
                    + "> and database version is <" + dbVersion + ">";
        } else {
            same = bonitaHomeVersion.equals(jarVersion);
            if (!same) {
                errorMessage = "The version of the bonita home is not the same as expected: bonita-server version is <" + jarVersion
                        + "> and bonita home version is <" + bonitaHomeVersion + ">";
            }
        }
        return same;
    }

    private String format(final String version) {
        final String trimVersion = version.trim();
        final int endIndex = trimVersion.indexOf('.', 2);
        if (endIndex == -1) {
            return trimVersion;
        }
        return trimVersion.substring(0, endIndex);
    }

    public SPlatform getPlatform() {
        return platform;
    }

    public SPlatformProperties getPlatformProperties() {
        return platformProperties;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
