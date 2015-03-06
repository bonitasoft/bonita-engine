/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.transaction.expression;

import org.bonitasoft.engine.api.impl.transaction.expression.bdm.ServerLazyLoader;
import org.bonitasoft.engine.api.impl.transaction.expression.bdm.ServerProxyfier;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;

import java.io.Serializable;
import java.util.Collection;


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
