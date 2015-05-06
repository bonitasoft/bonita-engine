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
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.Entity;

@RunWith(MockitoJUnitRunner.class)
public class MergeEntityActionTest {

    @Mock
    private BusinessDataRepository repository;

    @InjectMocks
    private MergeEntityAction mergeEntityAction;

    @Test
    public void execute_should_return_merged_entity() throws Exception {
        //given
        Entity entity = mock(Entity.class);
        Entity mergedEntity = mock(Entity.class);
        given(repository.merge(entity)).willReturn(mergedEntity);

        //when
        Entity actionResult = mergeEntityAction.execute(entity, null);

        //then
        assertThat(actionResult).isEqualTo(mergedEntity);
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throws_exception_if_entity_is_null() throws Exception {
        //when
        mergeEntityAction.execute((Entity) null, null);

        //then exception
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throws_exception_when_list_contains_null_elements() throws Exception {
        //when
        mergeEntityAction.execute(Arrays.asList(mock(Entity.class), null), null);

        //then exception
    }

    @Test
    public void execute_should_return_list_of_merged_entities() throws Exception {
        //given
        Entity entity1 = mock(Entity.class);
        Entity entity2 = mock(Entity.class);
        Entity mergedEntity1 = mock(Entity.class);
        Entity mergedEntity2 = mock(Entity.class);
        given(repository.merge(entity1)).willReturn(mergedEntity1);
        given(repository.merge(entity2)).willReturn(mergedEntity2);

        //when
        List<Entity> actionResult = mergeEntityAction.execute(Arrays.asList(entity1, entity2), null);

        //then
        assertThat(actionResult).containsExactly(mergedEntity1, mergedEntity2);
    }

}
