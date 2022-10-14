/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.server.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.bonitasoft.web.toolkit.server.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessActorImportServiceTest {

    @Test
    public void should_verify_authorisation_for_the_given_location_param() throws Exception {

        final ProcessActorImportService processActorImportService = spy(new ProcessActorImportService());
        doReturn(".." + File.separator + ".." + File.separator + ".." + File.separator + "file.txt")
                .when(processActorImportService).getFileUploadParameter();

        try {
            processActorImportService.run();
        } catch (final ServiceException e) {
            assertTrue(e.getMessage().startsWith("Unauthorized access to the file"));
        }
    }

}
