package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IsTaskAvailableForUserRuleTest {

    @Mock
    SessionAccessor sessionAccessor;

    @Mock
    SessionService sessionService;

    @Mock
    ActivityInstanceService activityInstanceService;

    @Mock
    SHumanTaskInstance humanTaskInstance;

    @Mock
    SAHumanTaskInstance archivedHumanTaskInstance;

    long userId = 2L;

    long taskInstanceId = 42L;

    @InjectMocks
    IsTaskAvailableForUserRule isTaskAvailableForUserRule = new IsTaskAvailableForUserRule(activityInstanceService, sessionService, sessionAccessor);

    @Before
    public void initMocks() throws Exception {
        when(sessionAccessor.getSessionId()).thenReturn(1L);
        SSession session = mock(SSession.class);
        when(session.getUserId()).thenReturn(userId);
        when(sessionService.getSession(1L)).thenReturn(session);
    }

    @Test
    public void isAllowed_should_return_true_if_is_assignee() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        when(humanTaskInstance.getAssigneeId()).thenReturn(userId);
        when(activityInstanceService.getHumanTaskInstance(taskInstanceId)).thenReturn(humanTaskInstance);

        assertThat(isTaskAvailableForUserRule.isAllowed("key", context)).isTrue();
    }

    @Test
    public void isAllowed_should_return_true_if_is_actor() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        when(humanTaskInstance.getAssigneeId()).thenReturn(0L);
        when(activityInstanceService.getHumanTaskInstance(taskInstanceId)).thenReturn(humanTaskInstance);
        when(activityInstanceService.isTaskPendingForUser(taskInstanceId, userId)).thenReturn(true);

        assertThat(isTaskAvailableForUserRule.isAllowed("key", context)).isTrue();
    }

    @Test
    public void isAllowed_should_return_false_if_is_assigneed_to_someone_else() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        when(humanTaskInstance.getAssigneeId()).thenReturn(5L);
        when(activityInstanceService.getHumanTaskInstance(taskInstanceId)).thenReturn(humanTaskInstance);

        assertThat(isTaskAvailableForUserRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void isAllowed_should_return_false_if_is_not_actor() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        when(humanTaskInstance.getAssigneeId()).thenReturn(0L);
        when(activityInstanceService.getHumanTaskInstance(taskInstanceId)).thenReturn(humanTaskInstance);
        when(activityInstanceService.isTaskPendingForUser(taskInstanceId, userId)).thenReturn(false);

        assertThat(isTaskAvailableForUserRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void isAllowed_should_return_false_if_archived_task_performed_by_someone_else() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        doThrow(SActivityInstanceNotFoundException.class).when(activityInstanceService).getHumanTaskInstance(taskInstanceId);
        when(archivedHumanTaskInstance.getExecutedBy()).thenReturn(5L);
        when(activityInstanceService.getLastArchivedFlowNodeInstance(SAHumanTaskInstance.class, taskInstanceId)).thenReturn(archivedHumanTaskInstance);

        assertThat(isTaskAvailableForUserRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void isAllowed_should_return_true_if_archived_task_performed_by_user() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        doThrow(SActivityInstanceNotFoundException.class).when(activityInstanceService).getHumanTaskInstance(taskInstanceId);
        when(archivedHumanTaskInstance.getExecutedBy()).thenReturn(userId);
        when(activityInstanceService.getLastArchivedFlowNodeInstance(SAHumanTaskInstance.class, taskInstanceId)).thenReturn(archivedHumanTaskInstance);

        assertThat(isTaskAvailableForUserRule.isAllowed("key", context)).isTrue();
    }

    @Test(expected = SExecutionException.class)
    public void isAllowed_should_throw_exception_when_task_not_found() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        final Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        queryParameters.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { Long.toString(taskInstanceId) });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParameters);
        doThrow(SActivityInstanceNotFoundException.class).when(activityInstanceService).getHumanTaskInstance(taskInstanceId);
        when(activityInstanceService.getLastArchivedFlowNodeInstance(SAHumanTaskInstance.class, taskInstanceId)).thenReturn(null);

        isTaskAvailableForUserRule.isAllowed("key", context);
    }

    @Test
    public void getIdShouldReturnIsTaskAvailableForUser() throws Exception {
        assertThat(isTaskAvailableForUserRule.getId()).isEqualTo("IS_TASK_AVAILABLE_FOR_USER");
    }

}
