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
package org.bonitasoft.web.rest.server.api.system;

import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.maintenance.MaintenanceDetails;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.system.MaintenanceDetailsClient;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/API/system/maintenance")
public class MaintenanceController extends AbstractRESTController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public MaintenanceDetails getMaintenanceDetails(HttpSession session) {
        APISession apiSession = getApiSession(session);
        try {
            return getMaintenanceAPI(apiSession).getMaintenanceDetails();
        } catch (BonitaException e) {
            String errorMessage = "Error while getting the maintenance info";
            log.error(errorMessage, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MaintenanceDetails changeMaintenanceState(@RequestBody MaintenanceDetailsClient maintenanceInfo,
            HttpSession session) {
        APISession apiSession = getApiSession(session);
        try {
            MaintenanceAPI maintenanceAPI = getMaintenanceAPI(apiSession);
            MaintenanceDetails currentMaintenanceDetails = maintenanceAPI.getMaintenanceDetails();
            if (maintenanceInfo.getMaintenanceState() != null
                    && currentMaintenanceDetails.getMaintenanceState() != maintenanceInfo.getMaintenanceState()) {
                //only enable/disable maintenance mode if needed
                if (MaintenanceDetails.State.ENABLED == maintenanceInfo.getMaintenanceState()) {
                    maintenanceAPI.enableMaintenanceMode();
                } else {
                    maintenanceAPI.disableMaintenanceMode();
                }
            }
            if (maintenanceInfo.isMaintenanceMessageActive() != null
                    && currentMaintenanceDetails.isMaintenanceMessageActive() != maintenanceInfo
                            .isMaintenanceMessageActive()) {
                //only update if different
                if (maintenanceInfo.isMaintenanceMessageActive()) {
                    maintenanceAPI.enableMaintenanceMessage();
                } else {
                    maintenanceAPI.disableMaintenanceMessage();
                }
            }
            if (maintenanceInfo.getMaintenanceMessage() != null
                    && currentMaintenanceDetails.getMaintenanceMessage() != maintenanceInfo
                            .getMaintenanceMessage()) {
                //only update if different
                maintenanceAPI.updateMaintenanceMessage(maintenanceInfo.getMaintenanceMessage());
            }
            return maintenanceAPI.getMaintenanceDetails();
        } catch (BonitaException e) {
            String errorMessage = "Error while setting the maintenance state";
            log.error(errorMessage, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }

    protected MaintenanceAPI getMaintenanceAPI(APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getMaintenanceAPI(apiSession);
    }
}
