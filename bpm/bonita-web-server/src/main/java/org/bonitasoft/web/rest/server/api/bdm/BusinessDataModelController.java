/**
 * Copyright (C) 2022-2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bdm;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.bdm.BusinessDataModelItem;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.bonitasoft.web.rest.server.api.SpringResponseEntityUtils;
import org.bonitasoft.web.rest.server.api.tenant.TenantResourceItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Anthony Birembaut
 * @author Emmanuel Duchastenier
 */
@RestController
@RequestMapping(path = "/API/tenant/bdm", produces = MediaType.APPLICATION_JSON_VALUE)
public class BusinessDataModelController extends AbstractRESTController {

    /**
     * @deprecated as of 9.0.0. The BDM should only be updated at startup.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated(since = "9.0.0")
    public TenantResourceItem installOrUpdateBDM(@RequestBody final BusinessDataModelItem businessDataModelItem,
            HttpSession httpSession)
            throws BonitaException {
        final TenantAdministrationAPI tenantAdministrationAPI = getTenantAdministrationAPI(httpSession);
        if (!tenantAdministrationAPI.isPaused()) {
            // Exception handling is done in the method right below:
            throw new TenantStatusException(
                    "Unable to install the Business Data Model. Please pause the BPM Services first. Go to Configuration > BPM Services.");
        }
        try {
            final FileContent businessDataModel = getBusinessDataModel(businessDataModelItem);
            final byte[] businessDataModelContent = getBusinessDataModelContent(businessDataModel.getInputStream());
            tenantAdministrationAPI.updateBusinessDataModel(businessDataModelContent);
            return new TenantResourceItem(tenantAdministrationAPI.getBusinessDataModelResource(),
                    businessDataModel.getFileName());
        } catch (final BusinessDataRepositoryDeploymentException e) {
            throw new APIException("An error has occurred when deploying Business Data Model.", e);
        } finally {
            getBonitaHomeFolderAccessor().removeUploadedTempContent(businessDataModelItem.getFileUpload());
        }
    }

    // In the particular case of this controller, we treat the TenantStatusException as a forbidden error.
    // In other controllers, it is treated in SpringRestResponseEntityExceptionHandler as a Service Unavailable error.
    @ExceptionHandler(value = { APIForbiddenException.class, TenantStatusException.class })
    protected ResponseEntity<Object> handleAPIForbidden(Exception ex) {
        return SpringResponseEntityUtils.generateErrorResponse(ex, HttpStatus.FORBIDDEN);
    }

    @GetMapping
    public TenantResourceItem getBDM(HttpSession httpSession) {
        try {
            return new TenantResourceItem(getTenantAdministrationAPI(httpSession).getBusinessDataModelResource());
        } catch (final TenantStatusException | InvalidSessionException e) {
            throw e; //handled by REST API Authorization filter
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    protected FileContent getBusinessDataModel(final BusinessDataModelItem item) {
        try {
            return getBonitaHomeFolderAccessor().retrieveUploadedTempContent(item.getFileUpload());
        } catch (final BonitaException e) {
            throw new APIException("Can't read business data model file", e);
        }
    }

    private byte[] getBusinessDataModelContent(InputStream inputStream) {
        try (inputStream) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new APIException("Can't read business data model file", e);
        }
    }

    protected BonitaHomeFolderAccessor getBonitaHomeFolderAccessor() {
        return new BonitaHomeFolderAccessor();
    }

}
