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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;

/**
 * @author Lu Kai
 * @author Baptiste Mesta
 */
public class CreateGroup implements TransactionContent {

    private final SGroup sGroup;

    private final IdentityService identityService;

    public CreateGroup(final SGroup sGroup, final IdentityService identityService) {
        this.sGroup = sGroup;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        try {
            identityService.getGroupByPath(sGroup.getPath());
            final String message = "Group named \"" + sGroup.getName() + "\" already exists";
            throw new SObjectAlreadyExistsException(message);
        } catch (final SGroupNotFoundException e) {
            identityService.createGroup(sGroup);
        }
    }

}
