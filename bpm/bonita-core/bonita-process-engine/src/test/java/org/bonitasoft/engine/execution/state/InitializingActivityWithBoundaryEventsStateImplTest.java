package org.bonitasoft.engine.execution.state;

import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializingActivityWithBoundaryEventsStateImplTest {

    @Mock
    private StateBehaviors stateBehaviors;

    @Mock
    private SProcessDefinition sProcessDefinition;

    @Mock
    private SFlowNodeInstance sFlowNodeInstance;

    @Test
    public void should_update_expected_duration() throws Exception {
        //given
        InitializingActivityWithBoundaryEventsStateImpl initializingActivityWithBoundaryEventsState = new InitializingActivityWithBoundaryEventsStateImpl(
                stateBehaviors);

        //when
        initializingActivityWithBoundaryEventsState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        //then
        verify(stateBehaviors).updateExpectedDuration(sProcessDefinition, sFlowNodeInstance);
    }

    @Test
    public void should_register_waiting_event_after_connectors() throws Exception {
        InitializingActivityWithBoundaryEventsStateImpl initializingActivityStateWithBoundary = new InitializingActivityWithBoundaryEventsStateImpl(stateBehaviors);

        initializingActivityStateWithBoundary.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        verify(stateBehaviors).registerWaitingEvent(sProcessDefinition, sFlowNodeInstance);
    }
}
