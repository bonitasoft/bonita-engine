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
package org.bonitasoft.engine.operation.pojo;

import org.bonitasoft.engine.bdm.Entity;

public class Travel implements Entity {

    private static final long serialVersionUID = 1L;

    private int nbDays;

    private Long persistenceId;

    private Long persistenceVersion;

    public int getNbDays() {
        return nbDays;
    }

    public void setNbDays(final int nbDays) {
        this.nbDays = nbDays;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(final Long id) {
        persistenceId = id;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

}
