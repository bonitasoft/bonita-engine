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
package org.bonitasoft.livingapps;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import org.bonitasoft.engine.business.application.IApplication;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.livingapps.exception.CreationException;
import org.bonitasoft.livingapps.menu.MenuFactory;

public class ApplicationModelFactory {

    private final ApplicationAPI applicationApi;
    private final PageAPI customPageApi;
    private final ProfileAPI profileApi;

    public ApplicationModelFactory(final ApplicationAPI applicationApi, final PageAPI customPageApi,
            final ProfileAPI profileApi) {
        this.applicationApi = applicationApi;
        this.customPageApi = customPageApi;
        this.profileApi = profileApi;
    }

    public ApplicationModel createApplicationModel(final String name) throws CreationException {

        try {
            final SearchResult<IApplication> result = applicationApi.searchIApplications(
                    new SearchOptionsBuilder(0, 1)
                            .filter(ApplicationSearchDescriptor.TOKEN, name)
                            .done());

            if (result.getCount() == 0) {
                throw new CreationException("No application found with name " + name);
            }
            // find a legacy application
            var legacyApplication = result.getResult().stream().filter(Application.class::isInstance)
                    .map(Application.class::cast).findFirst();
            if (legacyApplication.isEmpty()) {
                throw new CreationException("Only advanced applications were found with name " + name);
            }

            return new ApplicationModel(
                    applicationApi,
                    customPageApi,
                    profileApi,
                    legacyApplication.get(),
                    new MenuFactory(applicationApi));
        } catch (final SearchException e) {
            throw new CreationException("Error while searching for the application " + name, e);
        }
    }
}
