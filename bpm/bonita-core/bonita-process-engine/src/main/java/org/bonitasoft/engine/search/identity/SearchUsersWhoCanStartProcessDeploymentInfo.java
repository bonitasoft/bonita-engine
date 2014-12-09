/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.search.identity;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractUserSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchUserDescriptor;

/**
 * @author Celine Souchet
 * 
 */
public class SearchUsersWhoCanStartProcessDeploymentInfo extends AbstractUserSearchEntity {

    private final ProcessDefinitionService processDefinitionService;

    private final long processDefinitionId;

    public SearchUsersWhoCanStartProcessDeploymentInfo(final ProcessDefinitionService processDefinitionService, final SearchUserDescriptor searchDescriptor,
            final long processDefinitionId, final SearchOptions options) {
        super(searchDescriptor, options);
        this.processDefinitionService = processDefinitionService;
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return processDefinitionService.getNumberOfUsersWhoCanStartProcessDeploymentInfo(processDefinitionId, searchOptions);
    }

    @Override
    public List<SUser> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return processDefinitionService.searchUsersWhoCanStartProcessDeploymentInfo(processDefinitionId, searchOptions);
    }

}
