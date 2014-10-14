package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoDefinition;
import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;
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
        final CustomUserInfoDefinitionImpl definition = ModelConvertor.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void should_convert_server_value_into_client_value() {
        final CustomUserInfoValue value = ModelConvertor.convert(
                new DummySCustomUserInfoValue(2L, 2L, 1L, "value"));

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }

    @Test
    public void should_return_null_when_trying_to_convert_a_null_value() {
        final CustomUserInfoValue value = ModelConvertor.convert((SCustomUserInfoValue) null);

        assertThat(value).isNull();
    }
    @Test
    public void convertSContractDefinition() {
        //given
        final SimpleInputDefinition expectedSimpleInput = new SimpleInputDefinitionImpl("name", Type.TEXT, "description");
        final ComplexInputDefinition expectedComplexInput = new ComplexInputDefinitionImpl("complex input", "complex description",
                Arrays.asList(expectedSimpleInput), null);
        final ConstraintDefinition expectedRule = new ConstraintDefinitionImpl("name", "expression", "explanation");
        expectedRule.getInputNames().add("input1");
        expectedRule.getInputNames().add("input2");


        //when
        final SContractDefinition contractDefinition = new SContractDefinitionImpl();
        final SConstraintDefinition sRule = new SConstraintDefinitionImpl(expectedRule);
        final SSimpleInputDefinition sSimpleInput = new SSimpleInputDefinitionImpl(expectedSimpleInput);
        final SComplexInputDefinition sComplexInput = new SComplexInputDefinitionImpl(expectedComplexInput);

        contractDefinition.getConstraints().add(sRule);
        contractDefinition.getSimpleInputs().add(sSimpleInput);
        contractDefinition.getComplexInputs().add(sComplexInput);

        final ContractDefinition contract = ModelConvertor.toContract(contractDefinition);

        //then
        assertThat(contract.getConstraints()).as("should convert rules").containsExactly(expectedRule);
        assertThat(contract.getSimpleInputs()).as("should convert simple inputs").containsExactly(expectedSimpleInput);
        assertThat(contract.getComplexInputs()).as("should convert complex inputs").containsExactly(expectedComplexInput);
    }
}
