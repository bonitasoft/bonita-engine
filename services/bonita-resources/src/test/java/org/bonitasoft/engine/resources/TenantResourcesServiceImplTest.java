/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TenantResourcesServiceImplTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    @Mock
    Recorder recorder;
    @Mock
    ReadPersistenceService persistenceService;

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
    }

    @Test
    public void add_should_log_message_and_ignore_empty_file_content() throws Exception {
        // when
        tenantResourcesService.add("resourceName", TenantResourceType.BDM, new byte[] {}, -1);

        // then
        verifyZeroInteractions(recorder);
    }

    @Test
    public void add_should_work_for_valid_file_content() throws Exception {
        // when
        systemOutRule.clearLog();
        tenantResourcesService.add("resourceName", TenantResourceType.BDM, "someValidContent".getBytes(), -1);

        // then
        verify(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));
        assertThat(systemOutRule.getLog()).isEmpty();
    }

}
