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
package org.bonitasoft.engine.core.process.definition.model.event.trigger.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SLeftOperandBuilderFactory;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCorrelationDefinition;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Elias Ricken de Medeiros
 */
public class SCatchMessageEventTriggerDefinitionImpl extends SMessageEventTriggerDefinitionImpl implements SCatchMessageEventTriggerDefinition {

    private static final long serialVersionUID = 8502224424679479589L;

    private final List<SOperation> sOperations;

    public SCatchMessageEventTriggerDefinitionImpl(final String messageName, final List<SOperation> operations, final List<SCorrelationDefinition> correlations) {
        super(messageName, correlations);
        sOperations = operations;
    }

    public SCatchMessageEventTriggerDefinitionImpl() {
        sOperations = new ArrayList<SOperation>();
    }

    public SCatchMessageEventTriggerDefinitionImpl(final CatchMessageEventTriggerDefinition messageEventTrigger) {
        super(messageEventTrigger);
        final List<Operation> operations = messageEventTrigger.getOperations();
        sOperations = new ArrayList<SOperation>(operations.size());
        for (final Operation operation : operations) {
            sOperations.add(toSOperation(operation));
        }
    }

    public SCatchMessageEventTriggerDefinitionImpl(SCatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition) {
        super(catchMessageEventTriggerDefinition);
        sOperations = catchMessageEventTriggerDefinition.getOperations();
    }

    private SOperation toSOperation(final Operation operation) {
        final SExpression rightOperand = ServerModelConvertor.convertExpression(operation.getRightOperand());
        final SOperatorType operatorType = SOperatorType.valueOf(operation.getType().name());
        final SLeftOperand sLeftOperand = toSLeftOperand(operation.getLeftOperand());
        final SOperation sOperation = BuilderFactory.get(SOperationBuilderFactory.class).createNewInstance().setOperator(operation.getOperator())
                .setRightOperand(rightOperand).setType(operatorType).setLeftOperand(sLeftOperand).done();
        return sOperation;
    }

    private SLeftOperand toSLeftOperand(final LeftOperand variableToSet) {
        return BuilderFactory.get(SLeftOperandBuilderFactory.class).createNewInstance().setName(variableToSet.getName()).done();
    }

    @Override
    public List<SOperation> getOperations() {
        return Collections.unmodifiableList(sOperations);
    }

    public void addOperation(final SOperation operation) {
        sOperations.add(operation);
    }

}
