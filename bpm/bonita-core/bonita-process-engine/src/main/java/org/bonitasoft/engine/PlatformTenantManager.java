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
package org.bonitasoft.engine;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.platform.PlatformNotFoundException;

/**
 * @author Baptiste Mesta
 */
public class PlatformTenantManager {

    private static PlatformTenantManager instance;

    private static final String STOP = "stop";

    private static final String START = "start";

    protected PlatformTenantManager() {
    }

    public static synchronized PlatformTenantManager getInstance() {
        if (instance == null) {
            instance = new PlatformTenantManager();
        }
        return instance;
    }

    public boolean createPlatform(final PlatformAPI platformAPI) throws Exception {
        if (!platformAPI.isPlatformCreated()) {
            platformAPI.createAndInitializePlatform();
            return true;
        }
        return false;
    }

    private void updatePlatform(final PlatformAPI platformAPI, final String platformState) throws Exception {
        if (!platformAPI.isPlatformCreated()) {
            throw new PlatformNotFoundException("Can't start or stop platform if it is not created");
        }
        if (platformState.equals(START)) {
            platformAPI.startNode();
        } else if (platformState.equals(STOP)) {
            platformAPI.stopNode();
        }
    }

    public void startPlatform(final PlatformAPI platformAPI) throws Exception {
        updatePlatform(platformAPI, START);
    }

    public void stopPlatform(final PlatformAPI platformAPI) throws Exception {
        updatePlatform(platformAPI, STOP);
    }

}
