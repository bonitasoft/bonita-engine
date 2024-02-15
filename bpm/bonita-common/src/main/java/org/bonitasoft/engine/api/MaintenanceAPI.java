/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.maintenance.MaintenanceDetails;
import org.bonitasoft.engine.maintenance.MaintenanceDetailsNotFoundException;
import org.bonitasoft.engine.platform.PlatformNotFoundException;

/**
 * This API gives access to maintenance administration tasks such as enabling maintenance mode and also enable/disable
 * maintenance message.
 */
public interface MaintenanceAPI {

    /**
     * Retrieve platform maintenance details
     *
     * @return MaintenanceInfo
     * @throws MaintenanceDetailsNotFoundException
     * @throws PlatformNotFoundException
     */
    MaintenanceDetails getMaintenanceDetails() throws MaintenanceDetailsNotFoundException, PlatformNotFoundException;

    /**
     * Enable maintenance mode
     * This method replaces {@link TenantAdministrationAPI#pause()}
     * When maintenance mode is enabled, All BPM and BDM APIs are not accessible.
     *
     * @throws UpdateException
     *         if maintenance state cannot be updated.
     */
    void enableMaintenanceMode() throws UpdateException;

    /**
     * Disable maintenance mode
     * This method replaces {@link TenantAdministrationAPI#resume()}
     *
     * @throws UpdateException
     *         if maintenance state cannot be updated.
     */
    void disableMaintenanceMode() throws UpdateException;

    /**
     * Update maintenance message
     * This message will be displayed in bonita apps if enabled
     *
     * @throws UpdateException
     *         if maintenance message cannot be updated.
     */
    void updateMaintenanceMessage(String message) throws UpdateException;

    /**
     * Enable maintenance message
     *
     * @throws UpdateException
     *         if maintenance message cannot be enabled.
     */
    void enableMaintenanceMessage() throws UpdateException;

    /**
     * Disable maintenance message
     *
     * @throws UpdateException
     *         if maintenance message cannot be disabled.
     */
    void disableMaintenanceMessage() throws UpdateException;
}
