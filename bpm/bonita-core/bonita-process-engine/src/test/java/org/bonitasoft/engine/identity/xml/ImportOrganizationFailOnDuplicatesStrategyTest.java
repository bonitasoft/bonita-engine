package org.bonitasoft.engine.identity.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.junit.Test;

public class ImportOrganizationFailOnDuplicatesStrategyTest {

    @Test
    public void foundExistingCustomUserInfoDefinition_throws_ImportDuplicateInOrganizationException() {
        // given
        String name = "duplicate";
        SCustomUserInfoDefinition existingUserInfoDefinition = mock(SCustomUserInfoDefinition.class);
        CustomUserInfoDefinitionCreator newUserInfoDefinition = new CustomUserInfoDefinitionCreator(name);
        ImportOrganizationFailOnDuplicatesStrategy strategy = new ImportOrganizationFailOnDuplicatesStrategy();

        try {
            // when
            strategy.foundExistingCustomUserInfoDefinition(existingUserInfoDefinition, newUserInfoDefinition);
            fail("exception expected");
        } catch (ImportDuplicateInOrganizationException e) {
            // then
            assertThat(e.getMessage()).isEqualTo("There's already a custom user info definition with the name : '" + name + "'");
        }

    }

}
