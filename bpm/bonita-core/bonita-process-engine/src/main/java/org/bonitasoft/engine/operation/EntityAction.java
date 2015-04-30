/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package org.bonitasoft.engine.operation;

import java.util.List;

import com.bonitasoft.engine.bdm.Entity;

/**
 * @author Elias Ricken de Medeiros
 */
public interface EntityAction {

    /**
     * Executes an action against an entity.
     * 
     * @param entity the entity
     * @param businessDataContext the business data context
     * @return the entity after the action execution.
     */
    Entity execute(Entity entity, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException;

    /**
     * Executes an action against a a list of entities.
     * 
     * @param entities the list of entities
     * @param businessDataContext the business data context
     * @return the list of entities after the action execution.
     */
    List<Entity> execute(List<Entity> entities, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException;

}
