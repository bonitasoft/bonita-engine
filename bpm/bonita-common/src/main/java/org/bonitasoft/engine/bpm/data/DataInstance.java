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
package org.bonitasoft.engine.bpm.data;

import java.io.Serializable;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * This object represents this instance of {@link DataDefinition} generated when the associated
 * {@link org.bonitasoft.engine.bpm.process.ProcessInstance} or
 * {@link org.bonitasoft.engine.bpm.flownode.FlowNodeInstance} is instantiated.
 *
 * @author Feng Hui
 * @author Celine Souchet
 * @since 6.0.0
 * @version 6.4.1
 */
public interface DataInstance extends NamedElement, BaseElement {

    /**
     * Get the description of the data defined in {@link DataDefinition}.
     *
     * @return The description of the data.
     * @since 6.0.0
     * @see DataDefinition#getDescription()
     */
    String getDescription();

    /**
     * Get the class name of the type of the data defined in {@link DataDefinition}.
     *
     * @return The class name of the type of the data.
     * @since 6.0.0
     * @see DataDefinition#getClassName()
     */
    String getClassName();

    /**
     * Is it transient?
     *
     * @return <code>true</code> if the data is transient, <code>false</code> otherwise.
     * @since 6.0.0
     * @see DataDefinition#isTransientData()
     * @see #getTransientData()
     */
    Boolean isTransientData();

    /**
     * Is it transient? The canonical accessor for this property, and the one JavaBeans introspection and Groovy
     * property access resolve to - {@link #isTransientData()} returns the wrapper <code>Boolean</code>, and the
     * JavaBeans specification sanctions the <code>is</code> form only for a primitive <code>boolean</code>.
     * {@link #isTransientData()} is retained for existing callers.
     * <p>
     * See {@code doc/groovy_3_to_4-specific-study/} for why this alias exists and why the return type was not
     * narrowed instead.
     *
     * @return <code>true</code> if the data is transient, <code>false</code> otherwise.
     * @since 12.0.0
     * @see #isTransientData()
     */
    default Boolean getTransientData() {
        return isTransientData();
    }

    /**
     * Get the value of the data.
     *
     * @return The value of the data.
     * @since 6.0.0
     */
    Serializable getValue();

    /**
     * Get the identifier of the element where the data is defined. The element can be a
     * {@link org.bonitasoft.engine.bpm.process.ProcessInstance} or a
     * {@link org.bonitasoft.engine.bpm.flownode.FlowNodeInstance}.
     *
     * @return The identifier of the container of the data.
     * @since 6.0.0
     * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getId()
     * @see org.bonitasoft.engine.bpm.flownode.FlowNodeInstance#getId()
     */
    long getContainerId();

    /**
     * Get the type of the element where the data is defined.
     * The list of value for this field is :
     * <ul>
     * <li>
     * <code>PROCESS_INSTANCE</code>
     * </li>
     * <li>
     * <code>ACTIVITY_INSTANCE</code>
     * </li>
     * <li>
     * <code>MESSAGE_INSTANCE</code>
     * </li>
     * </ul>
     *
     * @return The type of the container of the data.
     * @since 6.0.0
     */
    String getContainerType();

}
