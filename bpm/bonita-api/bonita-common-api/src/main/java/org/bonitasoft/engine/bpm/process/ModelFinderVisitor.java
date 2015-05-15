/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.bpm.process;

import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * author Emmanuel Duchastenier
 */
public interface ModelFinderVisitor<T> {

    T find(Expression expression, long modelId);

    T find(final DesignProcessDefinition designProcessDefinition, final long modelId);

    T find(final FlowNodeDefinition flowNodeDefinition, final long modelId);

    T find(FlowElementContainerDefinition flowElementContainerDefinition, long modelId);

    T find(ActivityDefinition activityDefinition, long modelId);

    T find(CallActivityDefinition callActivityDefinition, long modelId);

    T find(Operation operation, long modelId);

    T find(TransitionDefinition transition, long modelId);
}
