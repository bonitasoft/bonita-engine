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

import java.io.File;
import java.io.IOException;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.UnauthorizedFolderException;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.exception.UnavailableLockException;
import org.bonitasoft.engine.io.IOUtil;
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
            final byte[] businessDataModelContent = getBusinessDataModelContent(businessDataModelItem);
            tenantAdministrationAPI.updateBusinessDataModel(businessDataModelContent);
            return new TenantResourceItem(tenantAdministrationAPI.getBusinessDataModelResource(),
                    businessDataModelItem.getFileUpload());
        } catch (APIForbiddenException e) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, e);
            return null;
        } catch (final InvalidBusinessDataModelException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e, "Invalid Business Data Model content");
            return null;
        } catch (final BusinessDataRepositoryDeploymentException e) {
            throw new APIException("An error has occurred when deploying Business Data Model.", e);
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
        }
    }

    @Get("json")
    public TenantResourceItem getBDM() {
        try {
            return new TenantResourceItem(tenantAdministrationAPI.getBusinessDataModelResource());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    public boolean isTenantPaused() {
        return tenantAdministrationAPI.isPaused();
    }

    private byte[] getBusinessDataModelContent(final BusinessDataModelItem item) {
        try {
            return IOUtil.getAllContentFrom(new File(getCompleteTempFilePath(item.getFileUpload())));
        } catch (final UnauthorizedFolderException e) {
            throw new APIForbiddenException(e.getMessage());
        } catch (final IOException e) {
            throw new APIException("Can't read business data model file", e);
        }
    }

    public String getCompleteTempFilePath(final String path) throws IOException {
        return bonitaHomeFolderAccessor.getCompleteTenantTempFilePath(path);
    }

}
