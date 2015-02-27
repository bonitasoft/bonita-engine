package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.junit.Test;

public class SSimpleInputDefinitionImplTest {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    @Test
    public void contructor_with_name() throws Exception {
        //given
        final SSimpleInputDefinitionImpl sSimpleInputDefinitionImpl = new SSimpleInputDefinitionImpl(NAME, SType.TEXT, DESCRIPTION);

        //then
        assertThat(sSimpleInputDefinitionImpl.isMultiple()).isFalse();
    }

    @Test
    public void constructor_with_input_definition() throws Exception {
        //given
        final SimpleInputDefinition simpleInput = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION, true);

        //when
        final SSimpleInputDefinitionImpl sSimpleInputDefinitionImpl = new SSimpleInputDefinitionImpl(simpleInput);

        //then
        assertThat(sSimpleInputDefinitionImpl.isMultiple()).isTrue();
        assertThat(sSimpleInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sSimpleInputDefinitionImpl.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(sSimpleInputDefinitionImpl.getType()).isEqualTo(SType.TEXT);

    }

}
