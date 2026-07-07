/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ActorDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class APIFlowNodeTest {

    @Mock
    private UserDatastore userDatastore;

    @Mock
    private ProcessDatastore processDatastore;

    @Mock
    private ActorDatastore actorDatastore;

    @Mock
    private CaseDatastore caseDatastore;

    private APIFlowNode apiFlowNode;

    @BeforeAll
    static void initEnvironment() {
        I18n.getInstance();
    }

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiFlowNode = spy(new APIFlowNode());
    }

    @Test
    void fillDeploys_should_fill_executor_when_deploy_of_executed_by_user_is_active() {
        // Given a flow node executed by a user that still exists in the organization
        doReturn(userDatastore).when(apiFlowNode).getUserDatastore();
        final APIID executorId = APIID.makeAPIID(3L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId... before the executedBy stub is matched
        lenient().doReturn(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID).when(item)
                .getAttributeValue(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID);
        doReturn(executorId).when(item).getExecutedByUserId();

        final List<String> deploys = List.of(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(executorId);

        // When
        apiFlowNode.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID, userItem);
    }

    @Test
    void fillDeploys_should_skip_executed_by_user_deploy_when_user_no_longer_exists() {
        // Given a flow node executed by a user that was deleted from the organization
        doReturn(userDatastore).when(apiFlowNode).getUserDatastore();
        final APIID deletedUserId = APIID.makeAPIID(3L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId... before the executedBy stub is matched
        lenient().doReturn(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID).when(item)
                .getAttributeValue(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID);
        doReturn(deletedUserId).when(item).getExecutedByUserId();

        final List<String> deploys = List.of(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When the unresolvable executor must not fail the whole task list
        assertThatCode(() -> apiFlowNode.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the executed_by deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID), any(Item.class));
    }

    @Test
    void fillDeploys_should_skip_executed_by_substitute_deploy_when_user_no_longer_exists() {
        // Given a flow node executed by a substitute user that was deleted from the organization
        doReturn(userDatastore).when(apiFlowNode).getUserDatastore();
        final APIID deletedUserId = APIID.makeAPIID(6L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId, executedBy... before this stub is matched
        lenient().doReturn(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID).when(item)
                .getAttributeValue(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID);
        doReturn(deletedUserId).when(item).getExecutedBySubstituteUserId();

        final List<String> deploys = List.of(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When the unresolvable substitute executor must not fail the whole task list
        assertThatCode(() -> apiFlowNode.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the executed_by_substitute deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID), any(Item.class));
    }

    @Test
    void fillDeploys_should_skip_assigned_user_deploy_when_user_no_longer_exists() {
        // Given a flow node assigned to a user that was deleted from the organization
        doReturn(userDatastore).when(apiFlowNode).getUserDatastore();
        final APIID deletedUserId = APIID.makeAPIID(3L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId, executedBy... before the assigned_id stub
        lenient().doReturn(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID).when(item)
                .getAttributeValue(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID);
        doReturn(deletedUserId).when(item).getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID);

        final List<String> deploys = List.of(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When the unresolvable assignee must not fail the whole task list
        assertThatCode(() -> apiFlowNode.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the assigned_id deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID), any(Item.class));
    }

    @Test
    void fillDeploys_should_fill_actor_when_deploy_of_actor_is_active() {
        // Given a task whose actor resolves.
        // Note: a dangling actor is not expected through the Engine Java API - a process definition
        // cannot be deleted while archived instances still exist (ProcessAPIImpl.deleteProcessDefinition
        // refuses), and actors are removed together with their process definition. The actor deploy is
        // nonetheless tolerated defensively should it ever be missing (see the actor-not-found test
        // below), while a genuine, non not-found fault still propagates (see the propagate test).
        doReturn(actorDatastore).when(apiFlowNode).getActorDatastore();
        final APIID actorId = APIID.makeAPIID(7L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId, executedBy... before the actorId stub
        lenient().doReturn(HumanTaskItem.ATTRIBUTE_ACTOR_ID).when(item)
                .getAttributeValue(HumanTaskItem.ATTRIBUTE_ACTOR_ID);
        doReturn(actorId).when(item).getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ACTOR_ID);

        final List<String> deploys = List.of(HumanTaskItem.ATTRIBUTE_ACTOR_ID);

        final ActorItem actorItem = new ActorItem();
        doReturn(actorItem).when(actorDatastore).get(actorId);

        // When
        apiFlowNode.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(HumanTaskItem.ATTRIBUTE_ACTOR_ID, actorItem);
    }

    @Test
    void fillDeploys_should_propagate_when_actor_resolution_fails_with_an_unexpected_error() {
        // Given the actor lookup fails with a generic (non not-found) error: ActorDatastore wraps
        // engine failures in a plain APIException. Such a genuine fault must NOT be silently swallowed -
        // deploySafely only tolerates the not-found types, so an unexpected error keeps propagating
        // instead of degrading the whole page to a 200 with blank fields.
        doReturn(actorDatastore).when(apiFlowNode).getActorDatastore();
        final APIID actorId = APIID.makeAPIID(7L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId, executedBy... before the actorId stub
        lenient().doReturn(HumanTaskItem.ATTRIBUTE_ACTOR_ID).when(item)
                .getAttributeValue(HumanTaskItem.ATTRIBUTE_ACTOR_ID);
        doReturn(actorId).when(item).getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ACTOR_ID);

        final List<String> deploys = List.of(HumanTaskItem.ATTRIBUTE_ACTOR_ID);

        doThrow(new APIException("actor resolution failed")).when(actorDatastore).get(actorId);

        // When / Then the unexpected error propagates (it is not a tolerated dangling reference)
        assertThatExceptionOfType(APIException.class)
                .isThrownBy(() -> apiFlowNode.fillDeploys(item, deploys));
        verify(item, never()).setDeploy(eq(HumanTaskItem.ATTRIBUTE_ACTOR_ID), any(ActorItem.class));
    }

    @Test
    void fillDeploys_should_skip_actor_deploy_when_actor_no_longer_exists() {
        // Given a task whose actor could not be resolved because it no longer exists. Not expected via
        // the Engine API today (actors are removed with their process definition), but tolerated
        // defensively like the other dangling references: a missing actor surfaces as an
        // APIItemNotFoundException whose cause is ActorNotFoundException.
        doReturn(actorDatastore).when(apiFlowNode).getActorDatastore();
        final APIID actorId = APIID.makeAPIID(7L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId, executedBy... before the actorId stub
        lenient().doReturn(HumanTaskItem.ATTRIBUTE_ACTOR_ID).when(item)
                .getAttributeValue(HumanTaskItem.ATTRIBUTE_ACTOR_ID);
        doReturn(actorId).when(item).getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ACTOR_ID);

        final List<String> deploys = List.of(HumanTaskItem.ATTRIBUTE_ACTOR_ID);

        doThrow(new APIItemNotFoundException(ActorItem.class.getName(), actorId,
                new ActorNotFoundException("actor deleted")))
                .when(actorDatastore).get(actorId);

        // When the unresolvable actor must not fail the whole task list
        assertThatCode(() -> apiFlowNode.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the actor deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(HumanTaskItem.ATTRIBUTE_ACTOR_ID), any(ActorItem.class));
    }

    @Test
    void fillDeploys_should_skip_process_deploy_when_process_no_longer_exists() {
        // Given a flow node whose process definition was deleted
        doReturn(processDatastore).when(apiFlowNode).getProcessDatastore();
        final APIID deletedProcessId = APIID.makeAPIID(9L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes caseId, executedBy... after the matched processId stub
        lenient().doReturn(FlowNodeItem.ATTRIBUTE_PROCESS_ID).when(item)
                .getAttributeValue(FlowNodeItem.ATTRIBUTE_PROCESS_ID);
        doReturn(deletedProcessId).when(item).getProcessId();

        final List<String> deploys = List.of(FlowNodeItem.ATTRIBUTE_PROCESS_ID);

        // A deleted process definition surfaces as APIItemNotFoundException (sibling of
        // APINotFoundException) - exercises the not-found catch of deploySafely.
        doThrow(new APIItemNotFoundException(ProcessItem.class.getName(), null,
                new ProcessDefinitionNotFoundException("process deleted")))
                .when(processDatastore).get(deletedProcessId);

        // When the unresolvable process must not fail the whole task list
        assertThatCode(() -> apiFlowNode.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the process deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(FlowNodeItem.ATTRIBUTE_PROCESS_ID), any(ProcessItem.class));
    }

    @Test
    void fillDeploys_should_skip_root_container_deploy_when_process_no_longer_exists() {
        // Given a flow node whose root-container case resolves, but its process definition was deleted
        doReturn(caseDatastore).when(apiFlowNode).getCaseDatastore();
        doReturn(processDatastore).when(apiFlowNode).getProcessDatastore();

        final APIID rootContainerId = APIID.makeAPIID(5L);
        final APIID deletedProcessId = APIID.makeAPIID(9L);
        final FlowNodeItem item = mock(FlowNodeItem.class);
        // lenient: fillDeploys probes processId, caseId... before the rootContainerId stub is matched
        lenient().doReturn(HumanTaskItem.ATTRIBUTE_ROOT_CONTAINER_ID).when(item)
                .getAttributeValue(HumanTaskItem.ATTRIBUTE_ROOT_CONTAINER_ID);
        doReturn(rootContainerId).when(item).getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ROOT_CONTAINER_ID);

        final CaseItem rootContainerCase = mock(CaseItem.class);
        doReturn(deletedProcessId).when(rootContainerCase).getProcessId();
        doReturn(rootContainerCase).when(caseDatastore).get(rootContainerId);

        final List<String> deploys = List.of(FlowNodeItem.ATTRIBUTE_ROOT_CONTAINER_ID);

        doThrow(new APIItemNotFoundException(ProcessItem.class.getName(), null,
                new ProcessDefinitionNotFoundException("process deleted")))
                .when(processDatastore).get(deletedProcessId);

        // When the unresolvable root-container process must not fail the whole task list
        assertThatCode(() -> apiFlowNode.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the root container deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(FlowNodeItem.ATTRIBUTE_ROOT_CONTAINER_ID), any(ProcessItem.class));
    }
}
