/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.api.impl.transaction.expression;

import java.io.Serializable;
import java.util.Collection;

import com.bonitasoft.engine.api.impl.transaction.expression.bdm.ServerLazyLoader;
import com.bonitasoft.engine.api.impl.transaction.expression.bdm.ServerProxyfier;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;


/**
 * @author Romain Bioteau
 *
 */
public class EntityMerger {

    private final BusinessDataRepository bdrService;

    public EntityMerger(final BusinessDataRepository bdrService) {
        this.bdrService = bdrService;
    }

    public Serializable merge(final Serializable value) {
        if (isACollectionOfEntities(value)) {
            final Collection<?> collection = (Collection<?>) value;
            try {
                @SuppressWarnings("unchecked")
                final Collection<Entity> newCollection = collection.getClass().newInstance();
                
                ServerProxyfier proxyfier = new ServerProxyfier(new ServerLazyLoader(bdrService));
                for (final Object item : collection) {
                    newCollection.add(proxyfier.proxify((Entity) item));
                }
                return (Serializable) newCollection;
            } catch (final InstantiationException e) {
                throw new IllegalStateException(e);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        } else if (isAnEntity(value)) {
            ServerProxyfier proxyfier = new ServerProxyfier(new ServerLazyLoader(bdrService));
            return proxyfier.proxify((Entity) value);
        } else {
            return value;
        }
    }

    private boolean isAnEntity(final Serializable value) {
        return value instanceof Entity;
    }

    protected boolean isACollectionOfEntities(final Serializable value) {
        return value instanceof Collection<?> && !((Collection<?>) value).isEmpty() && ((Collection<?>) value).iterator().next() instanceof Entity;
    }

}
