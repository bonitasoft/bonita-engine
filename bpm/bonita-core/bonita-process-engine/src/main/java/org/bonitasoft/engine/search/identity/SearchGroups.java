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
package org.bonitasoft.engine.search.identity;

import java.util.List;

import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractGroupSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchGroupDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public class SearchGroups extends AbstractGroupSearchEntity {

    private final IdentityService identityService;

    public SearchGroups(final IdentityService identityService, final SearchGroupDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.identityService = identityService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return identityService.getNumberOfGroups(searchOptions);
    }

    @Override
    public List<SGroup> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return identityService.searchGroups(searchOptions);
    }

}
