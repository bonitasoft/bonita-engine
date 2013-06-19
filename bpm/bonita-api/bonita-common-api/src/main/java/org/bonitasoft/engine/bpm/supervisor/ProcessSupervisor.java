/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
 * A process supervisor is the responsible for what happens to a specific process. A process supervisor can see the tasks of the process and do administration
 * of the process.
 * As for actors, process supervisors map people to processes through user / group / role associations.
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
