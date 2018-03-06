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
public class InitializingActivityStateImplTest {

    @Mock
    private StateBehaviors stateBehaviors;

    @Mock
    private SProcessDefinition sProcessDefinition;

    @Mock
    private SFlowNodeInstance sFlowNodeInstance;

    @Test
    public void should_update_expected_duration() throws Exception {
        //given
        InitializingActivityStateImpl initializingActivityState = new InitializingActivityStateImpl(stateBehaviors);

        //when
        initializingActivityState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        //then
        verify(stateBehaviors).updateExpectedDuration(sProcessDefinition, sFlowNodeInstance);
    }
}
