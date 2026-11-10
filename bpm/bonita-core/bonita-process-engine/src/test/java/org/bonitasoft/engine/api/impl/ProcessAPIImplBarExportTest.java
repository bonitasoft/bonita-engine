/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.bonitasoft.engine.bar.BusinessArchiveService;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.ProcessExportException;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessAPIImplBarExportTest {

    private static final long PROCESS_DEFINITION_ID = 42L;

    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private BusinessArchiveService businessArchiveService;
    @Mock
    private BusinessArchive businessArchive;
    @Spy
    private ProcessAPIImpl processAPI;

    @Test
    void exportBarProcessContentUnderHome_should_reject_business_archive_with_resource_outside_of_archive()
            throws Exception {
        //given
        doReturn(serviceAccessor).when(processAPI).getServiceAccessor();
        doReturn(businessArchiveService).when(serviceAccessor).getBusinessArchiveService();
        doReturn(businessArchive).when(businessArchiveService).export(PROCESS_DEFINITION_ID);
        doReturn(Map.of("resources/../../evil.txt", new byte[0])).when(businessArchive).getResources();

        //when-then
        assertThatThrownBy(() -> processAPI.exportBarProcessContentUnderHome(PROCESS_DEFINITION_ID))
                .isInstanceOf(ProcessExportException.class)
                .hasMessage("The business archive contains a resource whose path is outside of the archive");
    }
}
