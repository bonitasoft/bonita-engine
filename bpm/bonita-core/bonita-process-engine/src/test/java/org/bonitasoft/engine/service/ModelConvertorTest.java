package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAUserTaskInstanceImpl;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.execution.state.CompletedActivityStateImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.model.SUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModelConvertorTest {

    @Mock
    private FlowNodeStateManager manager;

    @Test
    public void convertDataInstanceIsTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(true);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertTrue(dataInstance.isTransientData());
    }

    @Test
    public void convertDataInstanceIsNotTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(false);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertFalse(dataInstance.isTransientData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnUnknownStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState("un_known_state");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnNullStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState(null);
    }

    @Test
    public void convertSUserToUserDoesntShowPassword() {
        final SUser sUser = mock(SUser.class);

        final User testUser = ModelConvertor.toUser(sUser);

        assertThat(testUser.getPassword()).isEmpty();
        verify(sUser, never()).getPassword();
    }

    @Test
    public void toArchivedUserTaskInstance_sould_return_the_right_idenfiers() throws Exception {
        final SAUserTaskInstanceImpl sInstance = new SAUserTaskInstanceImpl();
        sInstance.setRootContainerId(1L);
        sInstance.setParentContainerId(2L);
        sInstance.setLogicalGroup(0, 456789456798L);
        sInstance.setLogicalGroup(1, 1L);
        sInstance.setLogicalGroup(2, 456L);
        sInstance.setLogicalGroup(3, 2L);
        sInstance.setStateId(5);
        sInstance.setPriority(STaskPriority.NORMAL);

        when(manager.getState(5)).thenReturn(new CompletedActivityStateImpl());

        final ArchivedUserTaskInstance archivedUserTaskInstance = ModelConvertor.toArchivedUserTaskInstance(sInstance, manager);
        assertThat(archivedUserTaskInstance.getProcessDefinitionId()).isEqualTo(456789456798L);
        assertThat(archivedUserTaskInstance.getRootContainerId()).isEqualTo(1L);
        assertThat(archivedUserTaskInstance.getParentContainerId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getProcessInstanceId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getParentActivityInstanceId()).isEqualTo(456L);
    }

}
