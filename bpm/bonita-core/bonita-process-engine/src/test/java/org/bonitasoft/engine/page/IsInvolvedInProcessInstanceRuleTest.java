/*
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
 */
package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class IsInvolvedInProcessInstanceRuleTest extends RuleTest {

    @Mock
    ActivityInstanceService activityInstanceService;

    @InjectMocks
    IsInvolvedInProcessInstanceRule rule;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegallArgumentIfUserIdParamNotPresent() throws Exception {
        Map<String, Serializable> context = buildContext(24L, null);

        rule.isAllowed("key", context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegallArgumentIfProcessInstanceIdParamNotPresent() throws Exception {
        Map<String, Serializable> context = buildContext(null, 7L);

        rule.isAllowed("someKey", context);
    }

    @Test
    public void shouldNotAllowIfNotPendingOrAssignedTasks() throws Exception {
        Map<String, Serializable> context = buildContext(189L, 32L);
        doReturn(0L).when(activityInstanceService).getNumberOfPendingOrAssignedTasks(eq(32L), any(QueryOptions.class));

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isFalse();
    }

    @Test
    public void shouldAllowIfAtLeastOnePendingOrAssignedTask() throws Exception {
        Map<String, Serializable> context = buildContext(189L, 32L);
        doReturn(1L).when(activityInstanceService).getNumberOfPendingOrAssignedTasks(eq(32L), any(QueryOptions.class));

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isTrue();
    }

    @Test(expected = SExecutionException.class)
    public void shouldThrowExecutionExceptionIfReadException() throws Exception {
        Map<String, Serializable> context = buildContext(189L, 32L);
        doThrow(SBonitaReadException.class).when(activityInstanceService).getNumberOfPendingOrAssignedTasks(eq(32L), any(QueryOptions.class));

        rule.isAllowed("someKey", context);
    }

    @Test
    public void getIdShouldReturnIsInvolvedInProcessInstance() throws Exception {
        assertThat(rule.getId()).isEqualTo("IS_INVOLVED_IN_PROCESS_INSTANCE");
    }

}
