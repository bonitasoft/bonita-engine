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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.tenant.TenantResource;
import org.bonitasoft.engine.tenant.TenantResourceState;
import org.bonitasoft.engine.tenant.TenantResourceType;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class BusinessDataModelControllerTest extends AbstractControllerTest<BusinessDataModelController> {

    @Mock
    protected TenantAdministrationAPI tenantAdministrationAPI;

    private BusinessDataModelController controller;

    @Override
    protected BusinessDataModelController createController() {
        controller = spy(new BusinessDataModelController());
        return controller;
    }

    @Override
    protected void configureMocks(BusinessDataModelController controller) throws Exception {
        doReturn(tenantAdministrationAPI).when(controller).getTenantAdministrationAPI(any());
        doReturn(true).when(tenantAdministrationAPI).isPaused();
    }

    @Test
    void should_retrieve_bdm_from_engine_when_getting_bdm_from_api() throws Exception {
        long dateInMillis = new Date().getTime();
        final String formattedDate = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(dateInMillis),
                        ZoneOffset.UTC));
        when(tenantAdministrationAPI.getBusinessDataModelResource())
                .thenReturn(new TenantResource(1, "bdm.zip", TenantResourceType.BDM, dateInMillis, 12,
                        TenantResourceState.INSTALLED));

        mockMvc.perform(
                get("/API/tenant/bdm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "id":"1",
                            "name":"bdm.zip",
                            "type":"BDM",
                            "state":"INSTALLED",
                            "lastUpdatedBy":"12",
                            "lastUpdateDate":"%s",
                            "fileUpload":""
                        }
                        """.formatted(formattedDate), true));

        verify(tenantAdministrationAPI).getBusinessDataModelResource();
    }

    @Test
    void should_update_new_bdm() throws Exception {
        byte[] bdmFileContent = getContent("bizdatamodel.zip");
        final TenantResource tenantResource = new TenantResource(
                1L, "bizdatamodel", TenantResourceType.BDM, 1L, 1L,
                TenantResourceState.INSTALLED);
        doReturn(testBDMFile()).when(controller).getBusinessDataModel(any());
        doReturn("1.0").when(tenantAdministrationAPI).updateBusinessDataModel(bdmFileContent);
        doReturn(tenantResource).when(tenantAdministrationAPI).getBusinessDataModelResource();

        mockMvc.perform(
                post("/API/tenant/bdm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileUpload": "bizdatamodel"}""")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "id":"1",
                            "name":"bizdatamodel",
                            "type":"BDM",
                            "state":"INSTALLED",
                            "lastUpdatedBy":"1",
                            "lastUpdateDate":"1970-01-01T00:00:00.001Z",
                            "fileUpload":"bizdatamodel.zip"
                        }""", true));

        verify(tenantAdministrationAPI).updateBusinessDataModel(bdmFileContent);
    }

    private FileContent testBDMFile() {
        return new FileContent("bizdatamodel.zip",
                BusinessDataModelControllerTest.class.getResourceAsStream("bizdatamodel.zip"), "application/zip");
    }

    private byte[] getContent(String resource) throws IOException {
        try (final InputStream resourceAsStream = BusinessDataModelControllerTest.class.getResourceAsStream(resource)) {
            Assertions.assertNotNull(resourceAsStream);
            return IOUtils.toByteArray(resourceAsStream);
        }
    }

    @Test
    void install_should_throw_APIException_if_InvalidBusinessDataModelException_occurs() throws Exception {
        doReturn(testBDMFile()).when(controller).getBusinessDataModel(any());
        doThrow(new InvalidBusinessDataModelException(new Exception("invalid model"))).when(tenantAdministrationAPI)
                .updateBusinessDataModel(any(byte[].class));

        mockMvc.perform(
                post("/API/tenant/bdm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileUpload": "invalid bdm"}""")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(InvalidBusinessDataModelException.class.toString()))
                .andExpect(jsonPath("$.message").value("invalid model"));
    }

    @Test
    void should_throw_APIException_if_BusinessDataRepositoryDeploymentException_occurs()
            throws Exception {
        doReturn(testBDMFile()).when(controller).getBusinessDataModel(any());
        doThrow(new BusinessDataRepositoryDeploymentException("repository deployment exception"))
                .when(tenantAdministrationAPI)
                .updateBusinessDataModel(any());

        mockMvc.perform(
                post("/API/tenant/bdm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileUpload": "bizdatamodel"}""")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(APIException.class.toString()))
                .andExpect(jsonPath("$.message").value("repository deployment exception"));
    }

    @Test
    void cant_install_bdm_if_tenant_is_not_paused() throws Exception {
        doReturn(testBDMFile()).when(controller).getBusinessDataModel(any());
        doReturn(false).when(tenantAdministrationAPI).isPaused();

        mockMvc.perform(
                post("/API/tenant/bdm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileUpload": "bizdatamodel"}""")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(TenantStatusException.class.toString()))
                .andExpect(jsonPath("$.message").value(
                        "Unable to install the Business Data Model. Please pause the BPM Services first. " +
                                "Go to Configuration > BPM Services."));
    }
}
