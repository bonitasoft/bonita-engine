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
package org.bonitasoft.engine.bpm.flownode;

import java.util.List;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinition;

/**
 * A User Task is a typical “workflow” Task where a human performer performs the Task with the assistance of a
 * software application and is scheduled through a task list manager of some sort
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface UserTaskDefinition extends HumanTaskDefinition {

    /**
     * Contract that must be respected when executing an instance of this user task
     *
     * @return
     *         the user task execution contract
     */
    ContractDefinition getContract();



    List<ContextEntry> getContext();

}
