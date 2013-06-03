/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.api.impl.transaction.identity;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;

/**
 * @author Lu Kai
 */
public class GetSGroups implements TransactionContentWithResult<List<SGroup>> {

    private final List<Long> groupIds;

    private final IdentityService identityService;

    private List<SGroup> sGroups;

    public GetSGroups(final List<Long> groupIds, final IdentityService identityService) {
        this.groupIds = groupIds;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        sGroups = identityService.getGroups(groupIds);
    }

    @Override
    public List<SGroup> getResult() {
        return sGroups;
    }

}
