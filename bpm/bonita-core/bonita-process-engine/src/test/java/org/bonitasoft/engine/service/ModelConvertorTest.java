package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoDefinition;
import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SUser;
import org.junit.Test;

public class ModelConvertorTest {

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
    public void should_convert_server_definition_into_client_definition() {
        CustomUserInfoDefinitionImpl definition = ModelConvertor.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void should_convert_server_value_into_client_value() {
        CustomUserInfoValue value = ModelConvertor.convert(
                new DummySCustomUserInfoValue(2L, 2L, 1L, "value"));

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }

    @Test
    public void should_return_null_when_trying_to_convert_a_null_value() {
        CustomUserInfoValue value = ModelConvertor.convert((SCustomUserInfoValue) null);

        assertThat(value).isNull();
    }
}
