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
package org.bonitasoft.engine.tenant.restart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Collections;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class RestartFlowNodesHandlerTest {

    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @Mock
    private UserTransactionService transactionService;
    @Mock
    private ExecuteFlowNodes executeFlowNodes;
    private RestartFlowNodesHandler restartFlowNodesHandler;

    @Before
    public void before() {
        restartFlowNodesHandler = new RestartFlowNodesHandler(123L, logger, flowNodeInstanceService, transactionService,
                executeFlowNodes);
    }

    @Test
    public final void do_nothing_if_no_flownode() throws Exception {
        //given
        doReturn(Collections.EMPTY_LIST).when(flowNodeInstanceService)
                .getFlowNodeInstanceIdsToRestart(any(QueryOptions.class));

        //when
        restartFlowNodesHandler.beforeServicesStart();

        //then
        assertThat(restartFlowNodesHandler.flownodesToRestartByTenant.get(123l)).isEmpty();
    }

    @Test(expected = RestartException.class)
    public final void throw_exception_if_error_when_get_flownode() throws Exception {
        //given
        doThrow(new SBonitaReadException("plop")).when(flowNodeInstanceService)
                .getFlowNodeInstanceIdsToRestart(any(QueryOptions.class));

        //when
        restartFlowNodesHandler.beforeServicesStart();
    }
}
