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
package org.bonitasoft.engine.api.impl.transaction.identity;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 */
public class GetGroups implements TransactionContentWithResult<List<SGroup>> {

    private final IdentityService identityService;

    private final int startIndex;

    private final int maxResults;

    private final OrderByType orderExecutor;

    private final String fieldExecutor;

    private List<SGroup> groups;

    public GetGroups(final IdentityService identityService, final int startIndex, final int maxResults, final OrderByType orderExecutor,
            final String fieldExecutor) {
        this.identityService = identityService;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.orderExecutor = orderExecutor;
        this.fieldExecutor = fieldExecutor;
    }

    @Override
    public void execute() throws SBonitaException {
        if (fieldExecutor == null) {
            groups = identityService.getGroups(startIndex, maxResults);
        } else {
            groups = identityService.getGroups(startIndex, maxResults, fieldExecutor, orderExecutor);
        }
    }

    @Override
    public List<SGroup> getResult() {
        return groups;
    }

}
