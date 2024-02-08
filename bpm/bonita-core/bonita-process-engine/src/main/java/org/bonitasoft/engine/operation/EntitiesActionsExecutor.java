/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.operation;

import java.util.List;

import org.bonitasoft.engine.bdm.Entity;

/**
 * Allows the execution of an action against an Object that is instance of Entity or List<Entity>
 *
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
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
    public Object executeAction(final Object value, final BusinessDataContext businessDataContext,
            final EntityAction action) throws SEntityActionExecutionException {
        if (value == null) {
            action.handleNull(businessDataContext);
            return null;
        }
        if (value instanceof Entity) {
            return action.execute((Entity) value, businessDataContext);
        }
        if (value instanceof List<?>) {
            return action.execute((List<Entity>) value, businessDataContext);
        }
        throw new SEntityActionExecutionException(
                value.getClass().getName() + " is not a valid type. Expected an Entity or a List<Entity>");
    }

}
