/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.platform;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.execution.ProcessStarterVerifierImpl.LIMIT;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.bonitasoft.engine.execution.ProcessStarterVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformInformationAPIImplTest {

    @Mock
    private ProcessStarterVerifier processStarterVerifier;

    @InjectMocks
    private PlatformInformationAPIImpl platformInformationAPI;

    @Test
    void platformInformationAPI_should_return_case_counters_info() {
        //given
        doReturn(120L).when(processStarterVerifier).getCurrentNumberOfStartedProcessInstances();

        //when
        final Map<String, String> platformInformation = platformInformationAPI.getPlatformInformation();

        //then
        assertThat(platformInformation).containsAllEntriesOf(Map.of(
                "edition", "community",
                "caseCounter", "120",
                "caseCounterLimit", valueOf(LIMIT)));
    }
}
