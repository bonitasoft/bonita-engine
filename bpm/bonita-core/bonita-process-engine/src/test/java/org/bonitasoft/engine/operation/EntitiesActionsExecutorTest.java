/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.Container;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.Entity;

@RunWith(MockitoJUnitRunner.class)
public class EntitiesActionsExecutorTest {

    @InjectMocks
    private EntitiesActionsExecutor actionsExecutor;

    @Mock
    private EntityAction action;

    private BusinessDataContext businessDataContext = new BusinessDataContext("data", new Container(1L, "process"));

    @Test
    public void executeAction_should_execute_action_on_entity_if_its_a_simple_entity() throws Exception {
        //given
        Entity entity = mock(Entity.class);
        Entity entityAfterAction = mock(Entity.class);
        given(action.execute(entity, businessDataContext)).willReturn(entityAfterAction);

        //when
        Object actionResult = actionsExecutor.executeAction(entity, businessDataContext, action);

        //then
        assertThat(actionResult).isEqualTo(entityAfterAction);
        verify(action, never()).execute(anyListOf(Entity.class), any(BusinessDataContext.class));
    }

    @Test
    public void executeAction_should_execute_action_on_list_if_its_a_list_of_entities() throws Exception {
        //given
        List<Entity> entities = Collections.singletonList(mock(Entity.class));
        List<Entity> entitiesAfterAction = Collections.singletonList(mock(Entity.class));
        given(action.execute(entities, businessDataContext)).willReturn(entitiesAfterAction);

        //when
        Object actionResult = actionsExecutor.executeAction(entities, businessDataContext, action);

        //then
        assertThat(actionResult).isEqualTo(entitiesAfterAction);
        verify(action, never()).execute(any(Entity.class), any(BusinessDataContext.class));
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_action_should_throw_IllegalStateException_when_value_is_not_valid() throws Exception {
        //when
        actionsExecutor.executeAction("not entity, neither list of entity", businessDataContext, action);

        //then exception
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throws_exception_if_value_is_null() throws Exception {
        //when
        actionsExecutor.executeAction(null, businessDataContext, action);

        //then exception
    }

}
