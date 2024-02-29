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
package org.bonitasoft.console.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.console.server.service.OrganizationImportService;
import org.bonitasoft.console.server.service.ProcessActorImportService;
import org.bonitasoft.web.toolkit.server.Service;
import org.bonitasoft.web.toolkit.server.ServiceNotFoundException;
import org.junit.Test;

public class ConsoleServiceFactoryTest {

    @Test
    public void getService_should_return_OrganizationImportService_when_organization_import() throws Exception {
        // Given
        ConsoleServiceFactory consoleServiceFacotry = new ConsoleServiceFactory();

        // When
        Service service = consoleServiceFacotry.getService("/organization/import");

        // Then
        assertThat(service).isInstanceOf(OrganizationImportService.class);
    }

    @Test
    public void getService_should_return_ProcessActorImportService_when_bpm_process_importActors() throws Exception {
        // Given
        ConsoleServiceFactory consoleServiceFacotry = new ConsoleServiceFactory();

        // When
        Service service = consoleServiceFacotry.getService("/bpm/process/importActors");

        // Then
        assertThat(service).isInstanceOf(ProcessActorImportService.class);
    }

    @Test(expected = ServiceNotFoundException.class)
    public void getService_should_throw_ServiceNotFoundException_when_invalid_input() {
        // Given
        ConsoleServiceFactory consoleServiceFacotry = new ConsoleServiceFactory();

        // When
        consoleServiceFacotry.getService("invalidService");
    }

}
