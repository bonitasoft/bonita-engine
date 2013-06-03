/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.flownode.ManualTaskDefinition;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SManualTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SManualTaskDefinitionImpl extends SHumanTaskDefinitionImpl implements SManualTaskDefinition {

    private static final long serialVersionUID = 4800299070670205477L;

    /**
     * @param manualTaskDefinition
     * @param sExpressionBuilders
     * @param transitionsMap
     * @param sDataDefinitionBuilders
     * @param sOperationBuilders
     */
    public SManualTaskDefinitionImpl(final SFlowElementContainerDefinition parentContainer, final ManualTaskDefinition manualTaskDefinition,
            final SExpressionBuilders sExpressionBuilders, final Map<String, STransitionDefinition> transitionsMap,
            final SDataDefinitionBuilders sDataDefinitionBuilders, final SOperationBuilders sOperationBuilders) {
        super(parentContainer, manualTaskDefinition, sExpressionBuilders, transitionsMap, sDataDefinitionBuilders, sOperationBuilders);
    }

    /**
     * @param id
     * @param name
     * @param actorName
     */
    public SManualTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name, actorName);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.MANUAL_TASK;
    }

}
