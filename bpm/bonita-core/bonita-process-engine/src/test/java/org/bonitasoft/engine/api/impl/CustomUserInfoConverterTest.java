package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoConverterTest {

    @Test
    public void should_convert_server_definition_into_client_definition() throws Exception {
        CustomUserInfoConverter converter = new CustomUserInfoConverter();

        CustomUserInfoDefinitionImpl definition = converter.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "display name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDisplayName()).isEqualTo("display name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void should_convert_server_value_into_client_value() throws Exception {
        CustomUserInfoConverter converter = new CustomUserInfoConverter();

        CustomUserInfoValue value = converter.convert(
                new DummySCustomUserInfoValue(2L, 2L, 1L, "value"));

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }
}
