/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.business.data.impl;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.hibernate.proxy.HibernateProxyHelper;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataReloader {

    private final BusinessDataRepository businessDataRepository;

    public BusinessDataReloader(BusinessDataRepository businessDataRepository) {
        this.businessDataRepository = businessDataRepository;
    }

    /**
     * Reloads the {@link Entity} from database using the current {@code Entity} className and persistenceId
     *
     * @param entityToReload the entity to be reloaded
     * @return the {@link Entity} reload from the database
     * @throws SBusinessDataNotFoundException
     */
    public Entity reloadEntity(Entity entityToReload) throws SBusinessDataNotFoundException {
        final Class realClass = getEntityRealClass(entityToReload);
        return businessDataRepository.findById(realClass, entityToReload.getPersistenceId());
    }

    public Class getEntityRealClass(Entity entity) {
        return HibernateProxyHelper.getClassWithoutInitializingProxy(ServerProxyfier.unProxifyIfNeeded(entity));
    }

    /**
     * Reloads the {@link Entity} from database using the current {@code Entity} className and persistenceId if persistenceId is set. Otherwise returns the
     * object itself.
     *
     * @param entityToReload the entity to be reloaded
     * @return the {@link Entity} reload from the database if the persistenceId is set or the object itself.
     * @throws SBusinessDataNotFoundException
     */
    public Entity reloadEntitySoftly(Entity entityToReload) throws SBusinessDataNotFoundException {
        if (entityToReload.getPersistenceId() == null) {
            return entityToReload;
        }
        return reloadEntity(entityToReload);
    }

}
