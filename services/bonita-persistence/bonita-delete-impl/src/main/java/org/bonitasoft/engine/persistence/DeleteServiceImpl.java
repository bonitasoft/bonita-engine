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
package org.bonitasoft.engine.persistence;

import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * Default implementation of the delete service:
 * Just call the persistence service
 * 
 * @author Baptiste Mesta
 */
public class DeleteServiceImpl implements DeleteService {

    private final PersistenceService persistenceService;

    /**
     * @param persistenceService
     */
    public DeleteServiceImpl(final PersistenceService persistenceService) {
        super();
        this.persistenceService = persistenceService;
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        persistenceService.delete(entity);
    }

}
