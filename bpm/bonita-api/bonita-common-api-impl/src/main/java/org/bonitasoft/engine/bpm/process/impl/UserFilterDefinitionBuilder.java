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

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class UserFilterDefinitionBuilder extends FlowElementContainerBuilder {

    private final UserFilterDefinitionImpl userFilterDefinition;

    private final ProcessDefinitionBuilder processDefinitionBuilder;

    UserFilterDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container, final String name,
            final String filterId, final String version, final HumanTaskDefinitionImpl humanTaskDefinition) {
        super(container, processDefinitionBuilder);
        this.processDefinitionBuilder = processDefinitionBuilder;
        userFilterDefinition = new UserFilterDefinitionImpl(name, filterId, version);
        if (humanTaskDefinition.getUserFilter() != null) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to add a user filter on ");
            stringBuilder.append(humanTaskDefinition.getName());
            stringBuilder.append(", there is already one!");
            processDefinitionBuilder.addError(stringBuilder.toString());
        } else {
            humanTaskDefinition.setUserFilter(userFilterDefinition);
        }
    }

    /**
     * Adds a user filter input.
     * @param name input name.
     * @param value expression representing the input value.
     * @return
     */
    public UserFilterDefinitionBuilder addInput(final String name, final Expression value) {
        if (value == null) {
            processDefinitionBuilder.addError("The input " + name + " of user filter " + userFilterDefinition.getName() + " is null");
        }
        userFilterDefinition.addInput(name, value);
        return this;
    }

}
