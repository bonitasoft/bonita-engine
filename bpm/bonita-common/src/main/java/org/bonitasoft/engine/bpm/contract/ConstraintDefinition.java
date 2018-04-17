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
package org.bonitasoft.engine.bpm.contract;

import java.util.List;

import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;

/**
 * A <code>ConstraintDefinition</code> is a constraint when executing the {@link UserTaskInstance}.
 *
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 * @since 7.0
 */
public interface ConstraintDefinition extends NamedElement {

    /**
     * Returns the boolean condition used to validate a part of the {@link ContractDefinition}.
     * <p>
     * This expression will be evaluated at runtime when executing the {@link UserTaskInstance}.
     *
     * @return the boolean condition
     */
    String getExpression();

    /**
     * Returns the explanation of why the validation rule failed.
     *
     * @return the explanation of why the validation rule failed
     */
    String getExplanation();

    /**
     * Returns the input names involved in the validation rule.
     *
     * @return the input names involved in the validation rule
     */
    List<String> getInputNames();

}
