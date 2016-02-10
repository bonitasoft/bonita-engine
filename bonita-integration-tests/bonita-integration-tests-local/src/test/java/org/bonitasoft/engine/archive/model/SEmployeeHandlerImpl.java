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
package org.bonitasoft.engine.archive.model;

import java.util.UUID;

import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SUpdateEvent;

public class SEmployeeHandlerImpl implements SHandler<SUpdateEvent> {

    private static final long serialVersionUID = 1L;

    private static final String EMPLOYEE_UPDATED = "EMPLOYEE_UPDATED";

    private boolean isUpdated = false;

    private final String identifier;

    public SEmployeeHandlerImpl() {
        this.identifier = UUID.randomUUID().toString();
    }

    @Override
    public void execute(final SUpdateEvent updateEvent) {
        isUpdated = true;
    }

    @Override
    public boolean isInterested(final SUpdateEvent updateEvent) {
        if (updateEvent.getType().compareToIgnoreCase(EMPLOYEE_UPDATED) == 0) {
            final Employee newEmployee = (Employee) updateEvent.getObject();
            final Employee oldEmployee = (Employee) updateEvent.getOldObject();

            return !newEmployee.getName().equals(oldEmployee.getName());
        }
        return false;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(final boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
