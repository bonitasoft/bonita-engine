package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoDefinitionConverterTest {

    @Test
    public void should_convert_server_definition_into_client_definition() throws Exception {
        CustomUserInfoDefinitionConverter converter = new CustomUserInfoDefinitionConverter();

        CustomUserInfoDefinitionImpl definition = converter.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "display name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDisplayName()).isEqualTo("display name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }
}
