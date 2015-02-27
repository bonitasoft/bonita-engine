package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.junit.Test;


public class ComplexInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private ComplexInputDefinition complexInputDefinition;

    @Test
    public void constructor_multiple() throws Exception {
        complexInputDefinition = new ComplexInputDefinitionImpl(NAME, DESCRIPTION, true);

        //then
        assertThat(complexInputDefinition.isMultiple()).as("should be multiple").isTrue();
        assertThat(complexInputDefinition.getName()).as("should get name").isEqualTo(NAME);
        assertThat(complexInputDefinition.getDescription()).as("should get description").isEqualTo(DESCRIPTION);

    }

    @Test
    public void constructor_not_multiple() throws Exception {
        //given
        complexInputDefinition = new ComplexInputDefinitionImpl(NAME, DESCRIPTION, false);

        //then
        assertThat(complexInputDefinition.isMultiple()).as("should not be multiple").isFalse();
    }

    @Test
    public void toString_should_mention_multiple() throws Exception {
        //given
        complexInputDefinition = new ComplexInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(complexInputDefinition.toString()).containsIgnoringCase("multiple");
    }

}
