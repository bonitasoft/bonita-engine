/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.UserTaskInstanceBuilder.aUserTask;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.test.persistence.repository.FlowNodeInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Elias Ricken de Medeiros
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class FlowNodeInstanceTests {

    @Inject
    private FlowNodeInstanceRepository repository;

    @Test
    public void getFlowNodeInstanceIdsToRestart_should_return_ids_of_flow_nodes_that_are_not_deleted_and_is_executing_notStable_or_terminal() throws Exception {
        //given
        repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false)
                .build());
        repository.add(aUserTask().withName("deletedTask").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(true)
                .build());
        final SFlowNodeInstance executing = repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true).withTerminal(false)
                .withDeleted(false)
                .build());
        final SFlowNodeInstance notStable = repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true)
                .withDeleted(false)
                .build());
        final SFlowNodeInstance teminal = repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true).withTerminal(true)
                .withDeleted(false)
                .build());
        repository.add(aUserTask().withName("normalTask2").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false)
                .build());

        //when
        final QueryOptions options = new QueryOptions(0, 10);
        final List<Long> nodeToRestart = repository.getFlowNodeInstanceIdsToRestart(options);

        //then
        assertThat(nodeToRestart).hasSize(3);
        assertThat(nodeToRestart).contains(executing.getId(), notStable.getId(), teminal.getId());
    }

}
