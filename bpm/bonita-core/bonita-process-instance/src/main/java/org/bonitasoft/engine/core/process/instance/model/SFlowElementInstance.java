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

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public interface SFlowElementInstance extends SNamedElement {

    long getRootContainerId();

    long getParentContainerId();

    boolean isAborting();

    boolean isCanceling();

    SStateCategory getStateCategory();

    long getLogicalGroup(final int index);

    String getDescription();

    boolean isTerminal();

    boolean isStable();
    /**
     * @return
     *         the id of the process definition of this element
     */
    long getProcessDefinitionId();

    /**
     * the root process instance is the top level process containing this element
     * 
     * @return
     *         the id of the root process instance of this element
     */
    long getRootProcessInstanceId();

    /**
     * @return
     *         the id of the activity instance containing this element or 0 if this element is not contained in an activity
     */
    long getParentActivityInstanceId();

    /**
     * @return
     *         the id of the process instance containing this element
     */
    long getParentProcessInstanceId();

    /**
     * @return
     *         the type of the element that contains this element
     */
    SFlowElementsContainerType getParentContainerType();

}
