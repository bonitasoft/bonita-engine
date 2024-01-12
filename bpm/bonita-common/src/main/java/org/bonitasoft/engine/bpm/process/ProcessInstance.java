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
package org.bonitasoft.engine.bpm.process;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * Represents a running instance of a process.
 * Gives access to the information of the instance, whereas the state, the definition of the process instance...
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public interface ProcessInstance extends NamedElement, BaseElement {

    /**
     * Get the state of this process instance.
     *
     * @return The current state of this process instance.
     * @see ProcessInstanceState
     * @since 6.0.0
     */
    String getState();

    /**
     * Get the date when this process instance was started.
     *
     * @return The date when this process instance was started.
     * @since 6.0.0
     */
    Date getStartDate();

    /**
     * Get the identifier of the user who started this process instance.
     *
     * @return The identifier of the user who started this process instance.
     * @since 6.0.0
     */
    long getStartedBy();

    /**
     * Get the identifier of the substitute user (as Process manager or Administrator) who started this process
     * instance.
     *
     * @return The identifier of the substitute user (as Process manager or Administrator) who started this process
     *         instance.
     * @since 6.3.0
     */
    long getStartedBySubstitute();

    /**
     * Get the date when this process instance was finished.
     * It equals to null, if this process instance is not finished.
     *
     * @return The date when this process instance was finished.
     * @since 6.0.0
     */
    Date getEndDate();

    /**
     * Get the date of the last update of this process instance.
     *
     * @return The date of the last update of this process instance.
     * @since 6.0.0
     */
    Date getLastUpdate();

    /**
     * Get the identifier of the definition of this process.
     *
     * @return The identifier of the definition of this process.
     * @see ProcessDefinition#getId()
     * @since 6.0.0
     */
    long getProcessDefinitionId();

    /**
     * Get the description of this process instance.
     *
     * @return The description of this process instance.
     * @since 6.0.0
     */
    String getDescription();

    /**
     * Get the identifier of the root {@link ProcessInstance} of this process instance.
     * Is -1 if this process instance is not a child of another process instance.
     *
     * @return The identifier of the root process instance of this process instance.
     * @see ProcessInstance#getId()
     * @see ArchivedProcessInstance#getSourceObjectId()
     * @since 6.0.0
     */
    long getRootProcessInstanceId();

    /**
     * Get the identifier of the flow node instance who starts this process instance.
     * Is -1 if this process instance is not a child of another process instance.
     *
     * @return The identifier of the flow node instance who starts this process instance.
     * @see org.bonitasoft.engine.bpm.flownode.CallActivityInstance#getId()
     * @see SubProcessDefinition#getId()
     * @since 6.0.0
     */
    long getCallerId();

    /**
     * Also known as search key, this field is used to extend the meta data of the process instance.
     * It's the value corresponding to the search key label.
     * Return an empty string, if there is no first search key.
     *
     * @return The first search key of this process instance.
     * @see ProcessInstance#getStringIndexLabel(int)
     * @since 6.0.0
     */
    String getStringIndex1();

    /**
     * Also known as search key, this field is used to extend the meta data of the process instance.
     * It's the value corresponding to the search key label.
     * Return an empty string, if there is no second search key.
     *
     * @return The second search key of this process instance.
     * @see ProcessInstance#getStringIndexLabel(int)
     * @since 6.0.0
     */
    String getStringIndex2();

    /**
     * Also known as search key, this field is used to extend the meta data of the process instance.
     * It's the value corresponding to the search key label.
     * Return an empty string, if there is no third search key.
     *
     * @return The third search key of this process instance.
     * @see ProcessInstance#getStringIndexLabel(int)
     * @since 6.0.0
     */
    String getStringIndex3();

    /**
     * Also known as search key, this field is used to extend the meta data of the process instance.
     * It's the value corresponding to the search key label.
     * Return an empty string, if there is no fourth search key.
     *
     * @return The fourth search key of this process instance.
     * @see ProcessInstance#getStringIndexLabel(int)
     * @since 6.0.0
     */
    String getStringIndex4();

    /**
     * Also known as search key, this field is used to extend the meta data of the process instance.
     * It's the value corresponding to the search key label.
     * Return an empty string, if there is no fifth search key.
     *
     * @return The fifth search key of this process instance.
     * @see ProcessInstance#getStringIndexLabel(int)
     * @since 6.0.0
     */
    String getStringIndex5();

    /**
     * The index must be between 1 and 5.
     *
     * @param index
     *        The index of the label
     * @return The label of the search key corresponding to the parameter
     * @exception IndexOutOfBoundsException
     *            It's thrown if the parameter is not between 1 and 5.
     * @since 6.0.0
     */
    String getStringIndexLabel(int index);

}
