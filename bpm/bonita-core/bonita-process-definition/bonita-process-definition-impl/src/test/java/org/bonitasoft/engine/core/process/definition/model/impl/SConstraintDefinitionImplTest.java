package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintType;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintType;
import org.junit.Test;

public class SConstraintDefinitionImplTest {

    @Test
    public void should_retrieve_constraint_type() throws Exception {
        check_retrieve_constraint_type(SConstraintType.CUSTOM);
        check_retrieve_constraint_type(SConstraintType.MANDATORY);
    }

    @Test
    public void constructor_with_constraint_definition() throws Exception {
        check_constructor_from_constraintDefinition(ConstraintType.CUSTOM);
        check_constructor_from_constraintDefinition(ConstraintType.MANDATORY);
    }

    @Test
    public void should_add_input_name() throws Exception {
        //given
        final SConstraintDefinitionImpl sConstraintDefinition = new SConstraintDefinitionImpl("name", "expression", "explanation", SConstraintType.CUSTOM);

        //when
        sConstraintDefinition.addInputName("inputName");

        //then
        assertThat(sConstraintDefinition.getInputNames()).isNotNull().hasSize(1).containsExactly("inputName");

    }

    private void check_retrieve_constraint_type(final SConstraintType constraintType) {
        //given
        final SConstraintDefinition sConstraintDefinition = new SConstraintDefinitionImpl("name", "expression", "explanation", constraintType);

        //then
        assertThat(sConstraintDefinition.getConstraintType()).isEqualTo(constraintType);
        assertThat(sConstraintDefinition.getName()).isEqualTo("name");
        assertThat(sConstraintDefinition.getExpression()).isEqualTo("expression");
        assertThat(sConstraintDefinition.getExplanation()).isEqualTo("explanation");

        assertThat(sConstraintDefinition.getInputNames()).isNotNull().isEmpty();

    }

    private void check_constructor_from_constraintDefinition(final ConstraintType constraintType) {
        //given
        final ConstraintDefinition constraintDefinition = new ConstraintDefinitionImpl("name", "expression", "explanation", constraintType);
        constraintDefinition.getInputNames().add("inputName");

        //when
        final SConstraintDefinition sConstraintDefinition = new SConstraintDefinitionImpl(constraintDefinition);

        //then
        assertThat(sConstraintDefinition.getConstraintType().toString()).as("should retrieve constraint type").isEqualTo(constraintType.toString());
        assertThat(sConstraintDefinition.getName()).isEqualTo("name");
        assertThat(sConstraintDefinition.getExpression()).isEqualTo("expression");
        assertThat(sConstraintDefinition.getExplanation()).isEqualTo("explanation");

        assertThat(sConstraintDefinition.getInputNames()).isNotNull().hasSize(1).containsExactly("inputName");
    }
}
