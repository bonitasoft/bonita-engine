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

import org.bonitasoft.engine.api.impl.converter.ApplicationPageModelConverter;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchApplicationPages extends AbstractSearchEntity<ApplicationPage, SApplicationPage> {

    private final ApplicationService applicationService;
    private final ApplicationPageModelConverter convertor;

    public SearchApplicationPages(final ApplicationService applicationService,
            final ApplicationPageModelConverter convertor,
            final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.applicationService = applicationService;
        this.convertor = convertor;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.getNumberOfApplicationPages(queryOptions);
    }

    @Override
    public List<SApplicationPage> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.searchApplicationPages(queryOptions);
    }

    @Override
    public List<ApplicationPage> convertToClientObjects(final List<SApplicationPage> serverObjects) {
        return convertor.toApplicationPage(serverObjects);
    }

}
