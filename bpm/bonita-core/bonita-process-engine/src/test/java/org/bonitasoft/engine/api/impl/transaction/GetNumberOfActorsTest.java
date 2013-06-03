package org.bonitasoft.engine.api.impl.transaction;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfActors;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetNumberOfActorsTest {

    static ProcessDefinitionService processDefinitionService;
    static SProcessDefinition definition;
    static GetNumberOfActors getNumberOfActors;

    @BeforeClass
    public static void setUpClass() throws Exception {
        processDefinitionService  = mock(ProcessDefinitionService.class);
        definition = mock(SProcessDefinition.class);
        getNumberOfActors = new GetNumberOfActors(processDefinitionService, 1);
        when(processDefinitionService.getProcessDefinition(1)).thenReturn(definition);
    }

    @Test
    public void initiatorShouldBeCountOnlyOnceAsActor() throws Exception {
        SActorDefinition initiator = mock(SActorDefinition.class);
        when(definition.getActorInitiator()).thenReturn(initiator);
        when(definition.getActors()).thenReturn(Collections.singleton(initiator));

        getNumberOfActors.execute();
        assertThat(getNumberOfActors.getResult(), is(1));
    }

    @Test
    public void resultShouldBeOneWhenOnlyOneActorAndNoInitiator() throws Exception {
        SActorDefinition initiator = mock(SActorDefinition.class);
        when(definition.getActorInitiator()).thenReturn(null);
        when(definition.getActors()).thenReturn(Collections.singleton(initiator));

        getNumberOfActors.execute();
        assertThat(getNumberOfActors.getResult(), is(1));
    }

    @Test
    public void numberOfActorsShouldBeOneMoreIfInitiatorIsNotAnActor() throws Exception {
        SActorDefinition initiator = mock(SActorDefinition.class);
        SActorDefinition actor = mock(SActorDefinition.class);
        when(definition.getActorInitiator()).thenReturn(initiator);
        when(definition.getActors()).thenReturn(Collections.singleton(actor));

        getNumberOfActors.execute();
        assertThat(getNumberOfActors.getResult(), is(2));
    }
}
