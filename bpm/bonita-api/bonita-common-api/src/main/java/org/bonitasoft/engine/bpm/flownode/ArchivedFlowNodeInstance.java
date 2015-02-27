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

import org.bonitasoft.engine.bpm.ArchivedElement;
import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ArchivedFlowNodeInstance extends NamedElement, BaseElement, ArchivedElement {

    long getParentContainerId();

    long getRootContainerId();

    long getProcessDefinitionId();

    long getProcessInstanceId();

    /**
     * @return the parent activity instance id
     * @since 6.2
     */
    long getParentActivityInstanceId();

    String getState();

    FlowNodeType getType();

    String getDisplayName();

    String getDisplayDescription();

    /**
     * @return The identifier of the user who executed the flow node
     * @since 6.0.1
     */
    long getExecutedBy();

    /**
     * @return The identifier of the substitute user (as Process manager or Administrator) who executed the flow node.
     * @since 6.3.0
     */
    long getExecutedBySubstitute();

    /**
     * @return The identifier of the substitute user (as Process manager or Administrator) who executed the flow node.
     * @since 6.0.1
     * @deprecated since 6.3.0, use {@link ArchivedFlowNodeInstance#getExecutedBySubstitute()}
     */
    @Deprecated
    long getExecutedByDelegate();

    String getDescription();

    long getFlownodeDefinitionId();

    /**
     * @return true if this flow element is in a terminal state (= is finished)
     */
    boolean isTerminal();

}
