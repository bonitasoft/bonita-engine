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

import org.bonitasoft.console.server.service.ApplicationsImportService;
import org.bonitasoft.console.server.service.OrganizationImportService;
import org.bonitasoft.console.server.service.ProcessActorImportService;
import org.bonitasoft.web.toolkit.server.Service;
import org.bonitasoft.web.toolkit.server.ServiceFactory;
import org.bonitasoft.web.toolkit.server.ServiceNotFoundException;

public class ConsoleServiceFactory implements ServiceFactory {

    @Override
    public Service getService(final String calledToolToken) {
        if (OrganizationImportService.TOKEN.equals(calledToolToken)) {
            return new OrganizationImportService();
        } else if (ProcessActorImportService.TOKEN.equals(calledToolToken)) {
            return new ProcessActorImportService();
        } else if (ApplicationsImportService.TOKEN.equals(calledToolToken)) {
            return new ApplicationsImportService();
        }
        throw new ServiceNotFoundException(calledToolToken);
    }

}
