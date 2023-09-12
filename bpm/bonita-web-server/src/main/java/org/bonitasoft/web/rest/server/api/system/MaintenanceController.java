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
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.maintenance.MaintenanceInfo;
import org.bonitasoft.engine.session.APISession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/API/system/maintenance")
public class MaintenanceController {

    APISession getApiSession(HttpSession session) {
        APISession apiSession = (APISession) session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        if (apiSession == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return apiSession;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public MaintenanceInfo getMaintenanceInfo(HttpSession session) {
        APISession apiSession = getApiSession(session);
        try {
            return getMaintenanceAPI(apiSession).getMaintenanceInfo();
        } catch (BonitaException e) {
            String errorMessage = "Error while getting the maintenance info";
            log.error(errorMessage, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MaintenanceInfo changeMaintenanceState(@RequestBody MaintenanceInfoClient maintenanceInfo,
            HttpSession session) {
        APISession apiSession = getApiSession(session);
        try {
            MaintenanceAPI maintenanceAPI = getMaintenanceAPI(apiSession);
            MaintenanceInfo currentMaintenanceInfo = maintenanceAPI.getMaintenanceInfo();
            if (currentMaintenanceInfo.getMaintenanceState() != maintenanceInfo.getMaintenanceState()) {
                //only enable/disable maintenance mode if needed
                if (MaintenanceInfo.State.ENABLED == maintenanceInfo.getMaintenanceState()) {
                    maintenanceAPI.enableMaintenanceMode();
                } else {
                    maintenanceAPI.disableMaintenanceMode();
                }
            }
            if (currentMaintenanceInfo.isMaintenanceMessageActive() != maintenanceInfo
                    .isMaintenanceMessageActive()) {
                //only update if different
                if (maintenanceInfo.isMaintenanceMessageActive()) {
                    maintenanceAPI.enableMaintenanceMessage();
                } else {
                    maintenanceAPI.disableMaintenanceMessage();
                }
            }
            if (currentMaintenanceInfo.getMaintenanceMessage() != maintenanceInfo
                    .getMaintenanceMessage()) {
                //only update if different
                maintenanceAPI.updateMaintenanceMessage(maintenanceInfo.getMaintenanceMessage());
            }
            return maintenanceAPI.getMaintenanceInfo();
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
