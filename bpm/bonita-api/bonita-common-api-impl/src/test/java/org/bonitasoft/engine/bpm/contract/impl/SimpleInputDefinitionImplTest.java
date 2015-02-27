package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.junit.Test;



public class SimpleInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private SimpleInputDefinition simpleInputDefinition;
    private SimpleInputDefinition simpleInputDefinition2;

    @Test
    public void constructor_multiple() throws Exception {
        simpleInputDefinition = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION, true);

        //then
        assertThat(simpleInputDefinition.isMultiple()).as("should be multiple").isTrue();
        assertThat(simpleInputDefinition.getName()).as("should get name").isEqualTo(NAME);
        assertThat(simpleInputDefinition.getDescription()).as("should get description").isEqualTo(DESCRIPTION);

    }

    @Test
    public void constructor_not_multiple() throws Exception {
        //given
        simpleInputDefinition = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION, false);

        //then
        assertThat(simpleInputDefinition.isMultiple()).as("should not be multiple").isFalse();
    }

    @Test
    public void constructor_without_multiple() throws Exception {
        //given
        simpleInputDefinition = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION);

        //then
        assertThat(simpleInputDefinition.isMultiple()).as("should not be multiple").isFalse();
        assertThat(simpleInputDefinition.getType()).as("should get type").isEqualTo(Type.TEXT);
    }

    @Test
    public void toString_should_mention_multiple() throws Exception {
        //given
        simpleInputDefinition = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION);

        //then
        assertThat(simpleInputDefinition.toString()).containsIgnoringCase("multiple");
    }



    @Test
    public void equal_test() throws Exception {
        //given
        simpleInputDefinition = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION);
        simpleInputDefinition2 = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION);

        //then
        assertThat(simpleInputDefinition).isEqualTo(simpleInputDefinition2);
    }

    @Test
    public void not_equal_test() throws Exception {
        //given
        simpleInputDefinition = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION);
        simpleInputDefinition2 = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION, true);

        //then
        assertThat(simpleInputDefinition).isNotEqualTo(simpleInputDefinition2);
    }

}
