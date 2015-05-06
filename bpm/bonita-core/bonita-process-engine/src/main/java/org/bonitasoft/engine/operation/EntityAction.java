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

import java.util.List;

import org.bonitasoft.engine.bdm.Entity;

/**
 * @author Elias Ricken de Medeiros
 */
public interface EntityAction {

    /**
     * Executes an action against an entity.
     * 
     * @param entity the entity
     * @param businessDataContext the  business data context
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
