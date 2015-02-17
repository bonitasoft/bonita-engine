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

}
