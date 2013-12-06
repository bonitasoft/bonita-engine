/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;

/**
 * @author Matthieu Chaffotte
 */
public class CheckPlatformVersion implements TransactionContent {

    private final PlatformService platformService;

    private boolean same;

    private SPlatform platform;

    private SPlatformProperties platformProperties;

    public CheckPlatformVersion(final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Override
    public void execute() throws SBonitaException {
        platform = platformService.getPlatform();
        platformProperties = platformService.getSPlatformProperties();
        final String platformMinorVersion = format(platform.getVersion());
        final String propertiesMinorVersion = format(platformProperties.getPlatformVersion());
        same = platformMinorVersion.equals(propertiesMinorVersion);
    }

    private String format(final String version) {
        final String trimVersion = version.trim();
        final int endIndex = trimVersion.indexOf(".", 2);
        if (endIndex == -1) {
            return trimVersion;
        }
        return trimVersion.substring(0, endIndex);
    }

    public Boolean sameVersion() {
        return same;
    }

    public SPlatform getPlatform() {
        return platform;
    }

    public SPlatformProperties getPlatformProperties() {
        return platformProperties;
    }

}
