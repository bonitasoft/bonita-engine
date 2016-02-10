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
package org.bonitasoft.engine.api.impl.transaction.expression;

import java.io.Serializable;
import java.util.Collection;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.proxy.ServerLazyLoader;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;


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
            } catch (final InstantiationException | IllegalAccessException e) {
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
