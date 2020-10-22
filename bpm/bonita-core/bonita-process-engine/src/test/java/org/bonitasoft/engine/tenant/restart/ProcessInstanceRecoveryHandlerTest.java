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

import static java.util.Arrays.asList;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.FLOWNODE;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.PROCESS;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.builder;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Emmanuel Duchastenier
 */
@ExtendWith(MockitoExtension.class)
class ProcessInstanceRecoveryHandlerTest {

    @Mock
    private ProcessInstanceRecoveryService processInstanceRecoveryService;

    @InjectMocks
    private ProcessInstanceRecoveryHandler processInstanceRecoveryHandler;

    @Test
    void should_delegate_to_ProcessInstancesRecoveryService() {
        final List<ElementToRecover> elementToRecovers = asList(
                builder().id(17L).type(PROCESS).build(),
                builder().id(44L).type(FLOWNODE).build());
        doReturn(elementToRecovers).when(processInstanceRecoveryService).getAllElementsToRecover();

        processInstanceRecoveryHandler.beforeServicesStart();
        processInstanceRecoveryHandler.afterServicesStart();

        verify(processInstanceRecoveryService).recover(elementToRecovers);
    }
}
