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
package org.bonitasoft.engine.api.impl.transaction.application;

import java.util.List;

import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.IApplication;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

public class SearchApplicationsOfUser<E extends IApplication> extends AbstractSearchEntity<E, SApplication> {

    private final Class<E> applicationClass;
    private long userId;
    private final ApplicationService applicationService;
    private final ApplicationModelConverter convertor;

    public static SearchApplicationsOfUser<IApplication> defaultSearchApplicationsOfUser(
            final long userId,
            final ApplicationService applicationService,
            final SearchEntityDescriptor searchDescriptor,
            final SearchOptions options,
            final ApplicationModelConverter convertor) {
        return new SearchApplicationsOfUser<IApplication>(IApplication.class, userId, applicationService,
                searchDescriptor, options, convertor);
    }

    public SearchApplicationsOfUser(final Class<E> applicationClass, final long userId,
            final ApplicationService applicationService,
            final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final ApplicationModelConverter convertor) {
        super(searchDescriptor, options);
        this.applicationClass = applicationClass;
        this.userId = userId;
        this.applicationService = applicationService;
        this.convertor = convertor;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.getNumberOfApplicationsOfUser(userId, queryOptions);
    }

    @Override
    public List<SApplication> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.searchApplicationsOfUser(userId, queryOptions);
    }

    @Override
    public List<E> convertToClientObjects(final List<SApplication> serverObjects) {
        if (IApplication.class.equals(applicationClass)) {
            return (List<E>) convertor.toApplication(serverObjects);
        } else {
            return convertor.toApplication(serverObjects).stream().filter(applicationClass::isInstance)
                    .map(applicationClass::cast).toList();
        }
    }

}
