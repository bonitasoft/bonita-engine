/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.tenant.TenantElementsRestartSupervisor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecoverySchedulerTest {

    @Mock
    TenantElementsRestartSupervisor tenantElementsRestartSupervisor;
    @Mock
    RecoveryService recoveryService;
    @InjectMocks
    RecoveryScheduler recoveryScheduler;

    @Test
    void should_recover_elements_when_node_is_responsible_for_recovery() {
        when(tenantElementsRestartSupervisor.isResponsibleForRecovery()).thenReturn(true);

        recoveryScheduler.triggerRecoveryAllElements();

        verify(recoveryService).recoverAllElements();
    }

    @Test
    void should_not_recover_elements_when_node_is_not_responsible_for_recovery() {
        when(tenantElementsRestartSupervisor.isResponsibleForRecovery()).thenReturn(false);

        recoveryScheduler.triggerRecoveryAllElements();

        verify(recoveryService, never()).recoverAllElements();
    }

    @Test
    void should_catch_exception_on_error_during_call_of_isResponsibleForRecovery() {
        when(tenantElementsRestartSupervisor.isResponsibleForRecovery()).thenThrow(new IllegalStateException("BAD"));

        recoveryScheduler.triggerRecoveryAllElements();

        verify(recoveryService, never()).recoverAllElements();
    }

    @Test
    void should_catch_exception_on_error_during_calls_of_triggerRecoveryAllElements() {
        when(tenantElementsRestartSupervisor.isResponsibleForRecovery()).thenReturn(true);
        doThrow(new IllegalStateException("BAD")).when(recoveryService).recoverAllElements();

        recoveryScheduler.triggerRecoveryAllElements();

        //no exception
    }

}
