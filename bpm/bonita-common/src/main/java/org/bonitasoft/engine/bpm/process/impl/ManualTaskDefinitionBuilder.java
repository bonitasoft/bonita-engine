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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskDefinitionImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Feng Hui
 * @author Celine Souchet
 */
public class ManualTaskDefinitionBuilder extends ActivityDefinitionBuilder {

    public ManualTaskDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder,
            final FlowElementContainerDefinitionImpl container,
            final String name, final String actorName) {
        super(container, processDefinitionBuilder, getManualTask(name, actorName));
    }

    private static ManualTaskDefinitionImpl getManualTask(final String name, final String actorName) {
        return new ManualTaskDefinitionImpl(name, actorName);
    }

    /**
     * Adds a user filter on this manual task.
     *
     * @param name filter name in this task.
     * @param userFilterId user filter identifier.
     * @param version user filter version.
     * @return
     */
    public UserFilterDefinitionBuilder addUserFilter(final String name, final String userFilterId,
            final String version) {
        return new UserFilterDefinitionBuilder(getProcessBuilder(), getContainer(), name, userFilterId, version,
                (HumanTaskDefinitionImpl) getActivity());
    }

    /**
     * Sets the expected duration for this human task.
     *
     * @param time how long (in milliseconds) this task is expected to take.
     * @return
     * @deprecated use
     *             {@link org.bonitasoft.engine.bpm.process.impl.ManualTaskDefinitionBuilder#addExpectedDuration(org.bonitasoft.engine.expression.Expression)}
     **/
    public ManualTaskDefinitionBuilder addExpectedDuration(final long time) {
        try {
            final Expression expression = new ExpressionBuilder().createConstantLongExpression(time);
            return addExpectedDuration(expression);
        } catch (InvalidExpressionException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    /**
     * Sets the expected duration for this human task.
     *
     * @param expression how long (in milliseconds) this task is expected to take.
     * @return
     */
    public ManualTaskDefinitionBuilder addExpectedDuration(final Expression expression) {
        ((ManualTaskDefinitionImpl) getActivity()).setExpectedDuration(expression.copy());
        return this;
    }

    /**
     * Sets the task priority.
     *
     * @param priority task priority.
     * @return
     */
    public ManualTaskDefinitionBuilder addPriority(final String priority) {
        ((ManualTaskDefinitionImpl) getActivity()).setPriority(priority);
        return this;
    }

}
