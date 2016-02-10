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
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 */
public class GetUsersInGroup implements TransactionContentWithResult<List<SUser>> {

    private final long groupId;

    private final int startIndex;

    private final int maxResults;

    private final OrderByType orderExecutor;

    private final String fieldExecutor;

    private final IdentityService identityService;

    private List<SUser> userList;

    public GetUsersInGroup(final long groupId, final int startIndex, final int maxResults, final OrderByType orderExecutor, final String fieldExecutor,
            final IdentityService identityService) {
        this.groupId = groupId;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.orderExecutor = orderExecutor;
        this.fieldExecutor = fieldExecutor;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        if (fieldExecutor == null) {
            userList = identityService.getUsersByGroup(groupId, startIndex, maxResults);
        } else {
            userList = identityService.getUsersByGroup(groupId, startIndex, maxResults, fieldExecutor, orderExecutor);
        }
    }

    @Override
    public List<SUser> getResult() {
        return userList;
    }

}
