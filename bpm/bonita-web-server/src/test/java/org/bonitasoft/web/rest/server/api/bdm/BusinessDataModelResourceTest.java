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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.tenant.TenantResource;
import org.bonitasoft.engine.tenant.TenantResourceState;
import org.bonitasoft.engine.tenant.TenantResourceType;
import org.bonitasoft.web.rest.model.bdm.BusinessDataModelItem;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataModelResourceTest extends RestletTest {

    protected BusinessDataModelResource businessDataModelResource;

    @Mock
    protected TenantAdministrationAPI tenantAdministrationAPI;

    @Mock
    protected BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    @Mock
    protected APISession apiSession;

    @Override
    protected ServerResource configureResource() {
        businessDataModelResource = new BusinessDataModelResource(tenantAdministrationAPI, bonitaHomeFolderAccessor);
        return businessDataModelResource;
    }

    @Before
    public void init() {
        doReturn(true).when(tenantAdministrationAPI).isPaused();
    }

    @Test
    public void should_call_engine_when_we_try_to_get_bdm_ressource() throws Exception {
        long dateInMilis = new Date().getTime();
        when(tenantAdministrationAPI.getBusinessDataModelResource())
                .thenReturn(new TenantResource(1, "bdm.zip", TenantResourceType.BDM, dateInMilis, 12,
                        TenantResourceState.INSTALLED));

        final Response response = request("/tenant/bdm").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        verify(tenantAdministrationAPI).getBusinessDataModelResource();
    }

    @Test
    public void should_uninstall_and_install_bdm() throws Exception {
        final File bdmFile = testBDMFile();
        byte[] bdmFileContent = getContent(bdmFile);
        final BusinessDataModelItem item = new BusinessDataModelItem();
        item.setFileUpload(bdmFile.getName());
        final TenantResource tenantResource = new TenantResource(1L, "bizdatamodel", TenantResourceType.BDM, 1L, 1L,
                TenantResourceState.INSTALLED);
        final String jsonResponse = "{\"id\":\"1\",\"name\":\"bizdatamodel\",\"type\":\"BDM\",\"state\":\"INSTALLED\",\"lastUpdatedBy\":\"1\",\"lastUpdateDate\":\"1970-01-01T00:00:00.001Z\",\"fileUpload\":\"bizdatamodel.zip\"}";
        doReturn(bdmFile.getAbsolutePath()).when(bonitaHomeFolderAccessor)
                .getCompleteTenantTempFilePath(bdmFile.getName());
        when(tenantAdministrationAPI.updateBusinessDataModel(bdmFileContent)).thenReturn("1.0");
        when(tenantAdministrationAPI.getBusinessDataModelResource()).thenReturn(tenantResource);

        final Response response = request("/tenant/bdm").post(new ObjectMapper().writeValueAsString(item));

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        assertThat(response.getEntity().getText()).isEqualTo(jsonResponse);
        verify(tenantAdministrationAPI).updateBusinessDataModel(bdmFileContent);
    }

    private File testBDMFile() throws URISyntaxException {
        return new File(BusinessDataModelResourceTest.class.getResource("bizdatamodel.zip").toURI());
    }

    private byte[] getContent(final File businessDataModelFile) throws IOException, FileNotFoundException {
        final FileInputStream input = new FileInputStream(businessDataModelFile);
        try {
            return IOUtils.toByteArray(input);
        } finally {
            input.close();
        }
    }

    @Test
    public void should_throws_an_exception_adding_a_document_with_unauthorized_path() throws Exception {
        final BusinessDataModelItem item = new BusinessDataModelItem();
        item.setFileUpload("../../../bdmFile.zip");
        doThrow(new APIForbiddenException("")).when(bonitaHomeFolderAccessor)
                .getCompleteTenantTempFilePath("../../../bdmFile.zip");

        final Response response = request("/tenant/bdm").post(new ObjectMapper().writeValueAsString(item));

        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_FORBIDDEN);
    }

    @Test
    public void should_throws_an_exception_when_cannot_write_file_on_add() throws Exception {
        final BusinessDataModelItem item = new BusinessDataModelItem();
        item.setFileUpload("../../../bdmFile.zip");
        doThrow(new APIException("")).when(bonitaHomeFolderAccessor)
                .getCompleteTenantTempFilePath("../../../bdmFile.zip");

        final Response response = request("/tenant/bdm").post(new ObjectMapper().writeValueAsString(item));

        assertThat(response.getStatus()).isEqualTo(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void install_should_throw_APIException_if_InvalidBusinessDataModelException_occurs() throws Exception {
        final File bdmFile = testBDMFile();
        final BusinessDataModelItem item = new BusinessDataModelItem();
        item.setFileUpload(bdmFile.getName());
        doReturn(bdmFile.getAbsolutePath()).when(bonitaHomeFolderAccessor)
                .getCompleteTenantTempFilePath(bdmFile.getName());
        doThrow(new InvalidBusinessDataModelException(new Exception("invalid model"))).when(tenantAdministrationAPI)
                .updateBusinessDataModel(any(byte[].class));

        final Response response = request("/tenant/bdm").post(new ObjectMapper().writeValueAsString(item));

        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    @Test
    public void should_throw_APIException_if_BusinessDataRepositoryDeploymentException_occurs()
            throws Exception {
        final File bdmFile = testBDMFile();
        final BusinessDataModelItem item = new BusinessDataModelItem();
        item.setFileUpload(bdmFile.getName());
        doReturn(bdmFile.getAbsolutePath()).when(bonitaHomeFolderAccessor)
                .getCompleteTenantTempFilePath(bdmFile.getName());
        doThrow(new BusinessDataRepositoryDeploymentException("repository deployment exception"))
                .when(tenantAdministrationAPI)
                .updateBusinessDataModel(any());

        final Response response = request("/tenant/bdm").post(new ObjectMapper().writeValueAsString(item));

        assertThat(response.getStatus()).isEqualTo(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void cant_install_bdm_if_tenant_is_not_paused() throws Exception {
        final File bdmFile = testBDMFile();
        final BusinessDataModelItem item = new BusinessDataModelItem();
        item.setFileUpload(bdmFile.getName());

        doReturn(false).when(tenantAdministrationAPI).isPaused();

        final Response response = request("/tenant/bdm").post(new ObjectMapper().writeValueAsString(item));

        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_FORBIDDEN);
    }
}
