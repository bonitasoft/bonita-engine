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
package org.bonitasoft.engine.api.impl.transaction.application;

import java.util.List;

import org.bonitasoft.engine.api.impl.converter.ApplicationMenuModelConverter;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SearchApplicationMenus extends AbstractSearchEntity<ApplicationMenu, SApplicationMenu> {

    private final ApplicationService applicationService;
    private final ApplicationMenuModelConverter convertor;

    public SearchApplicationMenus(final ApplicationService applicationService, final ApplicationMenuModelConverter convertor,
            final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.applicationService = applicationService;
        this.convertor = convertor;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.getNumberOfApplicationMenus(queryOptions);
    }

    @Override
    public List<SApplicationMenu> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.searchApplicationMenus(queryOptions);
    }

    @Override
    public List<ApplicationMenu> convertToClientObjects(final List<SApplicationMenu> serverObjects) throws SBonitaException {
        return convertor.toApplicationMenu(serverObjects);
    }

}
