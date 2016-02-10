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
package org.bonitasoft.engine.bpm.supervisor;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * A supervisor of a process is responsible for what happens to the process. A supervisor can see
 * the tasks in the process, and can carry out process administration. A supervisor is defined in a ProcessSupervisor
 * object as a mapping of users, groups, or roles to the process supervisor (similar to actor mapping).
 * A process has one ProcessSupervisor; however, as this can be mapped to several users, either explicitly or by
 * mapping groups or roles, the process can be supervised by several people.
 * 
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public interface ProcessSupervisor extends BonitaObject {

    long getSupervisorId();

    long getProcessDefinitionId();

    long getUserId();

    long getGroupId();

    long getRoleId();

}
