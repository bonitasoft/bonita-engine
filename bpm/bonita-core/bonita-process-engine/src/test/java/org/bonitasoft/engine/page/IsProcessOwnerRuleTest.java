package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IsProcessOwnerRuleTest {

    @Mock
    SupervisorMappingService supervisorMappingService;

    @Mock
    SessionAccessor sessionAccessor;

    @Mock
    SessionService sessionService;

    @Mock
    FormMappingService formMappingService;

    @Mock
    SFormMapping formMapping;

    String pageMappingKey = "key";

    long processDefinitionId = 42L;

    long userId = 2L;

    @InjectMocks
    IsProcessOwnerRule isProcessOwnerRule = new IsProcessOwnerRule(supervisorMappingService, sessionAccessor, sessionService, formMappingService);

    @Before
    public void initMocks() throws Exception {
        when(formMappingService.get(pageMappingKey)).thenReturn(formMapping);
        when(formMapping.getProcessDefinitionId()).thenReturn(processDefinitionId);
        when(sessionAccessor.getSessionId()).thenReturn(1L);
        SSession session = mock(SSession.class);
        when(session.getUserId()).thenReturn(userId);
        when(sessionService.getSession(1L)).thenReturn(session);
    }

    @Test
    public void isAllowed_should_return_true_if_process_supervisor() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        when(supervisorMappingService.isProcessSupervisor(processDefinitionId, userId)).thenReturn(true);

        assertThat(isProcessOwnerRule.isAllowed(pageMappingKey, context)).isTrue();
    }

    @Test
    public void isAllowed_should_return_false_if_not_process_supervisor() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        when(supervisorMappingService.isProcessSupervisor(processDefinitionId, userId)).thenReturn(false);

        assertThat(isProcessOwnerRule.isAllowed(pageMappingKey, context)).isFalse();
    }

    @Test
    public void getIdShouldReturnIsProcessOwner() throws Exception {
        assertThat(isProcessOwnerRule.getId()).isEqualTo("IS_PROCESS_OWNER");
    }
}
