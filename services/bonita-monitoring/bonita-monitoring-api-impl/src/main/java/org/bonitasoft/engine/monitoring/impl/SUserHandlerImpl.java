/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.monitoring.impl;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public class SUserHandlerImpl implements SHandler<SEvent> {

    public static final String USER_CREATED = "USER_CREATED";

    public static final String USER_DELETED = "USER_DELETED";

    private long nbOfUsers = 0;

    public void setNbOfUsers(final long nbOfUsers) {
        this.nbOfUsers = nbOfUsers;
    }

    @Override
    public void execute(final SEvent event) {
        final String type = event.getType();
        if (USER_CREATED.compareToIgnoreCase(type) == 0) {
            nbOfUsers++;
        } else if (USER_DELETED.compareToIgnoreCase(type) == 0) {
            nbOfUsers--;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final String type = event.getType();
        if (USER_CREATED.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (USER_DELETED.compareToIgnoreCase(type) == 0) {
            return true;
        }
        return false;
    }

    public long getNbOfUsers() {
        return nbOfUsers;
    }

}
