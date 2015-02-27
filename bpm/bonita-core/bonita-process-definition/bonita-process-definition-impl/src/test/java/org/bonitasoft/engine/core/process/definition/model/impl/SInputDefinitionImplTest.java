package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";

    @Test
    public void constructor_without_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should not be multiple").isFalse();

    }

    @Test
    public void constructor_with_description_without_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should not be multiple").isFalse();

    }

    @Test
    public void constructor_with_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION, true);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should be multiple").isTrue();

    }

    @Test
    public void constructor_with_name_and_description() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.getName()).as("should get name").isEqualTo(NAME);
        assertThat(sInputDefinitionImpl.getDescription()).as("should get name").isEqualTo(DESCRIPTION);

    }

}
