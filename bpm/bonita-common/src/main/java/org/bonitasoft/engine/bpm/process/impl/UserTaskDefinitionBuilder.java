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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
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
public class UserTaskDefinitionBuilder extends ActivityDefinitionBuilder {

    public UserTaskDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String name, final String actorName) {
        super(container, processDefinitionBuilder, getUserTaskActivity(name, actorName));
    }

    private static UserTaskDefinitionImpl getUserTaskActivity(final String name, final String actorName) {
        return new UserTaskDefinitionImpl(name, actorName);
    }

    /**
     * Adds a user filter on this user task.
     * 
     * @param name filter name in this task.
     * @param userFilterId user filter identifier.
     * @param version user filter version.
     * @return
     */
    public UserFilterDefinitionBuilder addUserFilter(final String name, final String userFilterId, final String version) {
        return new UserFilterDefinitionBuilder(getProcessBuilder(), getContainer(), name, userFilterId, version, (HumanTaskDefinitionImpl) getActivity());
    }

    /**
     * Sets the expected duration for this human task.
     * see setExpectedDuration(expression)
     *
     * @param time how long (in milliseconds) this task is expected to take.
     * @return
     * @deprecated use {@link org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder#addExpectedDuration(org.bonitasoft.engine.expression.Expression)}
     */
    @Deprecated
    public UserTaskDefinitionBuilder addExpectedDuration(final long time) {
        try {
            final Expression expression = new ExpressionBuilder().createConstantLongExpression(time);
            return addExpectedDuration(expression);
        } catch (InvalidExpressionException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    /**
     * Sets the expected duration expression for this human task.
     *
     * @param expression return Long (in milliseconds) this task is expected to take.
     * @return
     */
    public UserTaskDefinitionBuilder addExpectedDuration(final Expression expression) {
        ((UserTaskDefinitionImpl) getActivity()).setExpectedDuration(expression.copy());
        return this;
    }

    /**
     * Sets the task priority.
     * 
     * @param priority task priority.
     * @return
     */
    public UserTaskDefinitionBuilder addPriority(final String priority) {
        ((UserTaskDefinitionImpl) getActivity()).setPriority(priority);
        return this;
    }

    public ContractDefinitionBuilder addContract() {
        return new ContractDefinitionBuilder(getProcessBuilder(), getContainer(), (UserTaskDefinitionImpl) getActivity());
    }

    public UserTaskDefinitionBuilder addContextEntry(String key, Expression expression) {
        ((UserTaskDefinitionImpl) getActivity()).addContextEntry(new ContextEntryImpl(key, expression));
        return this;
    }

}
