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
package org.bonitasoft.engine.search.supervisor;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSupervisorSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SearchSupervisors extends AbstractSupervisorSearchEntity {

    private final SupervisorMappingService supervisorService;

    public SearchSupervisors(final SupervisorMappingService supervisorService, final SearchEntityDescriptor searchDescriptor,
            final SearchOptions options) {
        super(searchDescriptor, options);
        this.supervisorService = supervisorService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return supervisorService.getNumberOfProcessSupervisors(searchOptions);
    }

    @Override
    public List<SProcessSupervisor> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return supervisorService.searchProcessSupervisors(searchOptions);
    }

}
