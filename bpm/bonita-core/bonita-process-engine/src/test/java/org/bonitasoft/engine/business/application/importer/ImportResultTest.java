/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.junit.Test;

public class ImportResultTest {

    @Test
    public void getImportedObject_should_return_object_passed_by_constructor() throws Exception {
        //given
        final SApplication application = mock(SApplication.class);
        final ImportResult importResult = new ImportResult(application, mock(ImportStatus.class));

        // when
        final SApplication retrievedApp = importResult.getApplication();

        //then
        assertThat(retrievedApp).isEqualTo(application);
    }

    @Test
    public void getImportStatus() throws Exception {
        //given
        final ImportStatus status = mock(ImportStatus.class);
        final ImportResult importResult = new ImportResult(mock(SApplication.class), status);

        //when
        final ImportStatus retrievedStatus = importResult.getImportStatus();

        //then
        assertThat(retrievedStatus).isEqualTo(status);
    }
}
