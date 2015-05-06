package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
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
public class IsActorInitiatorRuleTest {

    @Mock
    ActorMappingService actorMappingService;

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
    IsActorInitiatorRule isActorInitiatorRule = new IsActorInitiatorRule(actorMappingService, sessionAccessor, sessionService, formMappingService);

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
    public void isAllowed_should_return_true_if_actor_initiator() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        when(actorMappingService.canUserStartProcessDefinition(userId, processDefinitionId)).thenReturn(true);

        assertThat(isActorInitiatorRule.isAllowed(pageMappingKey, context)).isTrue();
    }

    @Test
    public void isAllowed_should_return_false_if_not_actor_initiator() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        when(actorMappingService.canUserStartProcessDefinition(userId, processDefinitionId)).thenReturn(false);

        assertThat(isActorInitiatorRule.isAllowed(pageMappingKey, context)).isFalse();
    }

    @Test
    public void getIdShouldReturnIsActorInitiator() throws Exception {
        assertThat(isActorInitiatorRule.getId()).isEqualTo("IS_ACTOR_INITIATOR");
    }
}
