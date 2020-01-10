/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.junit.Test;

public class ImportOrganizationFailOnDuplicatesStrategyTest {

    @Test
    public void foundExistingCustomUserInfoDefinition_throws_ImportDuplicateInOrganizationException() {
        // given
        String name = "duplicate";
        SCustomUserInfoDefinition existingUserInfoDefinition = mock(SCustomUserInfoDefinition.class);
        ExportedCustomUserInfoDefinition newUserInfoDefinition = new ExportedCustomUserInfoDefinition(name, null);
        ImportOrganizationFailOnDuplicatesStrategy strategy = new ImportOrganizationFailOnDuplicatesStrategy();

        try {
            // when
            strategy.foundExistingCustomUserInfoDefinition(existingUserInfoDefinition, newUserInfoDefinition);
            fail("exception expected");
        } catch (ImportDuplicateInOrganizationException e) {
            // then
            assertThat(e.getMessage())
                    .isEqualTo("There's already a custom user info definition with the name : '" + name + "'");
        }

    }

}
