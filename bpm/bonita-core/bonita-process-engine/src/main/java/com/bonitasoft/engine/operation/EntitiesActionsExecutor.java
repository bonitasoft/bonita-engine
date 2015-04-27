/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.operation;

import java.util.List;

import com.bonitasoft.engine.bdm.Entity;

/**
 * Allows the execution of an action against an Object that is instance of Entity or List<Entity>
 * 
 * @author Elias Ricken de Medeiros
 */
public class EntitiesActionsExecutor {

    /**
     * Executes of an action against an Object that is instance of Entity or List<Entity>.
     * 
     * @param value an Entity or List<Entity>
     * @param businessDataContext the business data context
     * @param action the action to be executed
     * @return the initial object after executing the action.
     */
    public Object executeAction(Object value, final BusinessDataContext businessDataContext, EntityAction action) throws SEntityActionExecutionException {
        if (value == null) {
            throw new SEntityActionExecutionException("Cannot execute action on null entity");
        }
        if (value instanceof Entity) {
            return action.execute(((Entity) value), businessDataContext);
        }
        if (value instanceof List<?>) {
            return action.execute((List<Entity>) value, businessDataContext);
        }
        throw new SEntityActionExecutionException(value.getClass().getName() + " is not a valid type. Expected an Entity or a List<Entity>");
    }

}
