/**
 * Copyright (C) 2022 Bonitasoft S.A.
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

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.exception.UnavailableLockException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.bdm.BusinessDataModelItem;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.rest.server.api.tenant.TenantResourceItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Anthony Birembaut
 */
public class BusinessDataModelResource extends CommonResource {

    private final TenantAdministrationAPI tenantAdministrationAPI;

    private final BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    public BusinessDataModelResource(final TenantAdministrationAPI tenantAdministrationAPI,
            BonitaHomeFolderAccessor bonitaHomeFolderAccessor) {
        this.bonitaHomeFolderAccessor = bonitaHomeFolderAccessor;
        this.tenantAdministrationAPI = tenantAdministrationAPI;
    }

    @Post("json")
    public TenantResourceItem addBDM(final BusinessDataModelItem businessDataModelItem) {
        if (!isTenantPaused()) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, new APIException(
                    "Unable to install the Business Data Model. Please pause the BPM Services first. Go to Configuration > BPM Services."));
            return null;
        }
        try {
            final FileContent businessDataModel = getBusinessDataModel(businessDataModelItem);
            final byte[] businessDataModelContent = getBusinessDataModelContent(businessDataModel.getInputStream());
            tenantAdministrationAPI.updateBusinessDataModel(businessDataModelContent);
            return new TenantResourceItem(tenantAdministrationAPI.getBusinessDataModelResource(),
                    businessDataModel.getFileName());
        } catch (APIForbiddenException e) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, e);
            return null;
        } catch (final InvalidBusinessDataModelException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e, "Invalid Business Data Model content");
            return null;
        } catch (final BusinessDataRepositoryDeploymentException e) {
            throw new APIException("An error has occurred when deploying Business Data Model.", e);
        } catch (final TenantStatusException | InvalidSessionException e) {
            throw e; //handled by REST API Authorization filter
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof UnavailableLockException) {
                // this request may be long and should make use of 202 status instead of 200
                // we return a 406 status here in order to prepare for this future API change
                setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, cause, cause.getMessage());
                return null;
            } else {
                throw e;
            }
        } finally {
            bonitaHomeFolderAccessor.removeUploadedTempContent(businessDataModelItem.getFileUpload());
        }
    }

    @Get("json")
    public TenantResourceItem getBDM() {
        try {
            return new TenantResourceItem(tenantAdministrationAPI.getBusinessDataModelResource());
        } catch (final TenantStatusException | InvalidSessionException e) {
            throw e; //handled by REST API Authorization filter
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    public boolean isTenantPaused() {
        return tenantAdministrationAPI.isPaused();
    }

    private FileContent getBusinessDataModel(final BusinessDataModelItem item) {
        try {
            return bonitaHomeFolderAccessor.retrieveUploadedTempContent(item.getFileUpload());
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

}
