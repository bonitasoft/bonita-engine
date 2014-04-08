package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.SBusinessDataDefinitionBuilderFactory;
import org.junit.Test;

public class SBusinessDataDefinitionBuilderFactoryImplTest {

    @Test
    public void getSBusinessDataDefinitionBuilderFactoryInterfaceShouldReturnsSBusinessDataDefinitionBuilderFactoryImpl() throws Exception {
        // when:
        SBusinessDataDefinitionBuilderFactory factory = BuilderFactory.get(SBusinessDataDefinitionBuilderFactory.class);
        // then:
        assertThat(factory).isInstanceOf(SBusinessDataDefinitionBuilderFactoryImpl.class);
    }
}
