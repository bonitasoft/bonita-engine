/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.process.instance.model;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * A token is an element that allows the tracking of active branch of the process.<br>
 * When the process starts, we create a token
 * When a gateway splits a branch, we create as many token as activated branch. these tokens are children of the token that activated the branch
 * When we merge branch, token are deleted and we take the parent token as the active one
 * 
 * @author Celine Souchet
 * @author Baptiste Mesta
 */
public interface SToken extends PersistentObject {

    long getProcessInstanceId();

    /**
     * @return
     *         the id of the element that created this token
     */
    Long getRefId();

    /**
     * @return
     *         this if of the element that created the parent token of this token
     */
    Long getParentRefId();

}
