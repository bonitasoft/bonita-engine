/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TenantResourcesServiceImplTest {

    @Mock
    Recorder recorder;
    @Mock
    ReadPersistenceService persistenceService;
    @Mock
    TechnicalLoggerService logger;

    @InjectMocks
    TenantResourcesServiceImpl tenantResourcesService;

    @Before
    public void initMocks() throws Exception {
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), anyString());
    }

    @Test
    public void add_should_log_message_and_ignore_null_file_content() throws Exception {
        // when
        tenantResourcesService.add("resourceName", TenantResourceType.BDM, null, -1);

        // then
        verifyZeroInteractions(recorder);
        verify(logger).log(TenantResourcesServiceImpl.class, TechnicalLogSeverity.WARNING,
                "Tenant resource file contains an empty file resourceName that will be ignored. Check that this is not a mistake.");
    }

    @Test
    public void add_should_log_message_and_ignore_empty_file_content() throws Exception {
        // when
        tenantResourcesService.add("resourceName", TenantResourceType.BDM, new byte[] {}, -1);

        // then
        verifyZeroInteractions(recorder);
        verify(logger).log(TenantResourcesServiceImpl.class, TechnicalLogSeverity.WARNING,
                "Tenant resource file contains an empty file resourceName that will be ignored. Check that this is not a mistake.");
    }

    @Test
    public void add_should_work_for_valid_file_content() throws Exception {
        // when
        tenantResourcesService.add("resourceName", TenantResourceType.BDM, "someValidContent".getBytes(), -1);

        // then
        verify(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));
        verifyZeroInteractions(logger);
    }

}
