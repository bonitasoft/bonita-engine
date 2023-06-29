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
package org.bonitasoft.engine.bpm.contract;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;

/**
 * A <code>ContractDefinition</code> defines what the <code>UserTaskInstance</code> needs to be executed, or the Process
 * to be started.
 * <p>
 * A <code>ContractDefinition</code> is part of a {@link UserTaskDefinition} or a of the
 * {@link org.bonitasoft.engine.bpm.process.ProcessDefinition} </p>
 *
 * @author Matthieu Chaffotte
 * @since 7.0
 */
public interface ContractDefinition extends InputContainerDefinition {

    /**
     * Lists the validation rules of the contract.
     *
     * @return the validation rules of the contract
     */
    List<ConstraintDefinition> getConstraints();

}
