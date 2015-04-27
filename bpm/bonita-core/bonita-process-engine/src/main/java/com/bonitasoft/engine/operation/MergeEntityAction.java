/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.operation;

import java.util.ArrayList;
import java.util.List;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.proxy.ServerProxyfier;

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
        return repository.merge(ServerProxyfier.unProxyfyIfNeeded(entity));
    }

    @Override
    public List<Entity> execute(final List<Entity> entities, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        List<Entity> mergedEntities = new ArrayList<Entity>();
        for (Entity entity : entities) {
            mergedEntities.add(execute(entity, businessDataContext));
        }
        return mergedEntities;
    }
}
