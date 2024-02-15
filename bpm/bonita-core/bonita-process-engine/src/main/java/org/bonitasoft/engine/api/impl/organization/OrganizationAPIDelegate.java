/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.organization;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.*;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;

/**
 * @author Emmanuel Duchastenier
 */
public class OrganizationAPIDelegate {

    private static OrganizationAPIDelegate organizationAPIDelegate = null;
    private final IdentityService identityService;

    private OrganizationAPIDelegate(IdentityService identityService) {
        this.identityService = identityService;
    }

    public static OrganizationAPIDelegate getInstance() {
        if (organizationAPIDelegate == null) {
            organizationAPIDelegate = new OrganizationAPIDelegate(ServiceAccessorSingleton.getInstance()
                    .getIdentityService());
        }
        return organizationAPIDelegate;
    }

    public List<String> importOrganizationWithWarnings(String organizationContent, ImportPolicy policy)
            throws OrganizationImportException {
        try {
            final SCustomUserInfoValueUpdateBuilderFactory updaterFactor = BuilderFactory
                    .get(SCustomUserInfoValueUpdateBuilderFactory.class);
            final SCustomUserInfoValueAPI customUserInfoValueAPI = new SCustomUserInfoValueAPI(
                    identityService, updaterFactor);
            ImportOrganization importedOrganization = new ImportOrganization(identityService, organizationContent,
                    policy, customUserInfoValueAPI);
            return importedOrganization.execute();
        } catch (JAXBException e) {
            throw new InvalidOrganizationFileFormatException(e);
        } catch (final SBonitaException e) {
            throw new OrganizationImportException(e);
        }
    }
}
