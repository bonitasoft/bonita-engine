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
package org.bonitasoft.engine.business.application.impl.cleaner;

import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.filter.FilterBuilder;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuCleaner {

    private ApplicationService applicationService;

    public ApplicationMenuCleaner(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void deleteRelatedApplicationMenus(FilterBuilder filterBuilder) throws SBonitaException {
        QueryOptions options = filterBuilder.buildQueryOptions();
        List<SApplicationMenu> relatedMenus;
        do {
            relatedMenus = applicationService.searchApplicationMenus(options);
            for (SApplicationMenu relatedMenu : relatedMenus) {
                applicationService.deleteApplicationMenu(relatedMenu);
            }
        } while (relatedMenus.size() == options.getNumberOfResults());

    }

}
