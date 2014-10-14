package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintType;
import org.junit.Test;

public class ConstraintDefinitionImplTest {

    @Test
    public void should_custom_type_be_default_constraint_type() throws Exception {
        //given
        final ConstraintDefinition constraintDefinition = new ConstraintDefinitionImpl("name", "expression", "explanation");

        //then
        assertThat(constraintDefinition.getConstraintType()).isEqualTo(ConstraintType.CUSTOM);
    }

    @Test
    public void should_retrieve_constraint_type() throws Exception {
        //given
        final ConstraintDefinition constraintDefinition = new ConstraintDefinitionImpl("name", "expression", "explanation", ConstraintType.CUSTOM);

        //then
        assertThat(constraintDefinition.getConstraintType()).isEqualTo(ConstraintType.CUSTOM);
        assertThat(constraintDefinition.getName()).isEqualTo("name");
        assertThat(constraintDefinition.getExpression()).isEqualTo("expression");
        assertThat(constraintDefinition.getExplanation()).isEqualTo("explanation");

        assertThat(constraintDefinition.getInputNames()).isNotNull().isEmpty();

    }


    @Test
    public void should_add_input_name() throws Exception {
        //given
        final ConstraintDefinitionImpl constraintDefinition = new ConstraintDefinitionImpl("name", "expression", "explanation", ConstraintType.CUSTOM);

        //when
        constraintDefinition.addInputName("inputName");

        //then
        assertThat(constraintDefinition.getInputNames()).isNotNull().hasSize(1).containsExactly("inputName");

    }
}
