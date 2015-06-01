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
package org.bonitasoft.engine.operation;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;

/**
 * @author Elias Ricken de Medeiros
 */
public class MergeEntityAction implements EntityAction {

    BusinessDataRepository repository;

    public MergeEntityAction(final BusinessDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Entity execute(final Entity entity, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        if (entity == null) {
            throw new SEntityActionExecutionException("Unable to insert/update a null business data");
        }
        try {
            return repository.merge(ServerProxyfier.unProxy(entity));
        } catch (final IllegalArgumentException | IllegalAccessException iae) {
            throw new SEntityActionExecutionException(iae);
        }
    }

    @Override
    public List<Entity> execute(final List<Entity> entities, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        final List<Entity> mergedEntities = new ArrayList<>();
        for (final Entity entity : entities) {
            mergedEntities.add(execute(entity, businessDataContext));
        }
        return mergedEntities;
    }

}
