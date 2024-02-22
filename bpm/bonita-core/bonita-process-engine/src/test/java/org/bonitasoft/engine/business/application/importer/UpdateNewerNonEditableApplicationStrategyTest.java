/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */

public class UpdateNewerNonEditableApplicationStrategyTest {

    private UpdateNewerNonEditableApplicationStrategy strategy = new UpdateNewerNonEditableApplicationStrategy();

    @Test
    public void whenApplicationExists_should_return_Replace_when_existing_application_is_not_editable_and_version_different()
            throws Exception {
        // given:
        final long applicationId = 123L;
        final SApplication existingApplication = new SApplication();
        existingApplication.setId(applicationId);
        existingApplication.setEditable(false);
        existingApplication.setVersion("v1");

        final SApplicationWithIcon applicationToImport = new SApplicationWithIcon();
        applicationToImport.setId(98745L);
        applicationToImport.setEditable(false);
        applicationToImport.setVersion("v2");
        // when:
        ApplicationImportStrategy.ImportStrategy importStrategy = strategy.whenApplicationExists(existingApplication,
                applicationToImport);

        // then:
        assertThat(importStrategy).isEqualTo(ApplicationImportStrategy.ImportStrategy.REPLACE);
    }

    @Test
    public void whenApplicationExists_should_return_skip_when_existing_application_is_not_editable_and_version_equals()
            throws Exception {
        // given:
        final long applicationId = 123L;
        final SApplication existingApplication = new SApplication();
        existingApplication.setId(applicationId);
        existingApplication.setEditable(false);
        existingApplication.setVersion("v1");

        final SApplicationWithIcon applicationToImport = new SApplicationWithIcon();
        applicationToImport.setId(98745L);
        applicationToImport.setEditable(false);
        applicationToImport.setVersion("v1");
        // when:
        ApplicationImportStrategy.ImportStrategy importStrategy = strategy.whenApplicationExists(existingApplication,
                applicationToImport);

        // then:
        assertThat(importStrategy).isEqualTo(ApplicationImportStrategy.ImportStrategy.SKIP);
    }

    @Test
    public void whenApplicationExists_should_return_skip_when_existing_application_is_editable() throws Exception {
        // given:
        final long applicationId = 123L;
        final SApplication existingApplication = new SApplication();
        existingApplication.setId(applicationId);
        existingApplication.setEditable(true);
        existingApplication.setVersion("v1");

        final SApplicationWithIcon applicationToImport = new SApplicationWithIcon();
        applicationToImport.setId(98745L);
        applicationToImport.setEditable(true);
        applicationToImport.setVersion("v2");
        // when:
        ApplicationImportStrategy.ImportStrategy importStrategy = strategy.whenApplicationExists(existingApplication,
                applicationToImport);

        // then:
        assertThat(importStrategy).isEqualTo(ApplicationImportStrategy.ImportStrategy.SKIP);
    }
}
