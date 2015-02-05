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
package org.bonitasoft.engine.bpm.userfilter;

import java.util.Map;

import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.expression.Expression;

/**
 * Design-time object of a UserFilter (also known as actor filter).
 * A UserFilter is a derogation of the standard actor mapping mechanism.
 * If a human task has a userfilter, its execution determines the list of users that the task is pending for. Other users will not see it as available for them.
 * <code>UserFilter</code>s have many similarities to connector in their definition and execution.
 *
 * @author Baptiste Mesta
 * @see org.bonitasoft.engine.bpm.connector.ConnectorDefinition
 */
public interface UserFilterDefinition extends NamedElement {

    /**
     * @return the ID of its definition.
     */
    String getUserFilterId();

    /**
     * @return the version of its definition.
     */
    String getVersion();

    /**
     * @return the map of expressions that serves as input for the execution of the user filter.
     */
    Map<String, Expression> getInputs();

}
