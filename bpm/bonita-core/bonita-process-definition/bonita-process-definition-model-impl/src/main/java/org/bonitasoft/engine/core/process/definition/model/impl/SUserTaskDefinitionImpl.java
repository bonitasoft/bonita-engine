/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserTaskDefinitionImpl extends SHumanTaskDefinitionImpl implements SUserTaskDefinition {

    private static final long serialVersionUID = 9039679250456947450L;

    public SUserTaskDefinitionImpl(final SFlowElementContainerDefinition parentContainer, final UserTaskDefinition userTaskDefinition,
            final SExpressionBuilders sExpressionBuilders, final Map<String, STransitionDefinition> transitionsMap,
            final SDataDefinitionBuilders sDataDefinitionBuilders, final SOperationBuilders sOperationBuilders) {
        super(parentContainer, userTaskDefinition, sExpressionBuilders, transitionsMap, sDataDefinitionBuilders, sOperationBuilders);
    }

    public SUserTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name, actorName);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.USER_TASK;
    }

}
