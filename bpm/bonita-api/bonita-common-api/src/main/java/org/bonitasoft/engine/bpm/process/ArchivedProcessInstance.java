/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process;

import java.util.Date;

import org.bonitasoft.engine.bpm.ArchivedElement;
import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ArchivedProcessInstance extends NamedElement, BaseElement, ArchivedElement {

    String getState();

    Date getStartDate();

    /**
     * @return id of the user who originally started the process
     * @since 6.0.1
     */
    long getStartedBy();

    /**
     * @return id of the user (delegate) who started the process for the original starter
     * @since 6.0.1
     */
    long getStartedByDelegate();

    Date getEndDate();

    Date getLastUpdate();

    long getProcessDefinitionId();

    String getDescription();

    long getRootProcessInstanceId();

    long getCallerId();

    int getStateId();

    long getSourceObjectId();

}
