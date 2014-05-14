/**
 * Copyright (C) 2012, 2014 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
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

import java.io.Serializable;
import java.util.Date;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.impl.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.StandardLoopCharacteristics;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;

/**
 * @author Matthieu Chaffotte
 */
public class ActivityDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final ActivityDefinitionImpl activity;

    public ActivityDefinitionBuilder(final FlowElementContainerDefinitionImpl container, final ProcessDefinitionBuilder processDefinitionBuilder,
            final ActivityDefinitionImpl activity) {
        super(container, processDefinitionBuilder);
        this.activity = activity;
        getContainer().addActivity(activity);
    }

    ActivityDefinitionImpl getActivity() {
        return activity;
    }

    @Override
    public DataDefinitionBuilder addIntegerData(final String name, final Expression defaultValue) {
        final String className = Integer.class.getName();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addLongData(final String name, final Expression defaultValue) {
        final String className = Long.class.getName();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addShortTextData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new TextDataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public TextDataDefinitionBuilder addLongTextData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new TextDataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue).isLongText();
    }

    @Override
    public DataDefinitionBuilder addDoubleData(final String name, final Expression defaultValue) {
        final String className = Double.class.getName();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addDateData(final String name, final Expression defaultValue) {
        final String className = Date.class.getName();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public XMLDataDefinitionBuilder addXMLData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new XMLDataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addBlobData(final String name, final Expression defaultValue) {
        final String className = Serializable.class.getName();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addBooleanData(final String name, final Expression defaultValue) {
        final String className = Boolean.class.getName();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addData(final String name, final String className, final Expression defaultValue) {
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, className, defaultValue);
    }

    @Override
    public ConnectorDefinitionBuilder addConnector(final String name, final String connectorId, final String version, final ConnectorEvent activationEvent) {
        return new ConnectorDefinitionBuilder(getProcessBuilder(), getContainer(), name, connectorId, version, activationEvent, activity);
    }

    @Override
    public ActivityDefinitionBuilder addDescription(final String description) {
        activity.setDescription(description);
        return this;
    }

    public ActivityDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        activity.setDisplayDescription(displayDescription);
        return this;
    }

    public ActivityDefinitionBuilder addDisplayName(final Expression displayName) {
        activity.setDisplayName(displayName);
        return this;
    }

    public ActivityDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        activity.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

    public ActivityDefinitionBuilder addOperation(final LeftOperand leftOperand, final OperatorType type, final String operator,
            final String operatorInputType, final Expression rightOperand) {
        final Operation operation = new OperationBuilder().createNewInstance().setRightOperand(rightOperand).setType(type).setOperator(operator)
                .setOperatorInputType(operatorInputType).setLeftOperand(leftOperand).done();
        return addOperation(operation);
    }

    public ActivityDefinitionBuilder addOperation(final Operation operation) {
        activity.addOperation(operation);
        if (operation.getRightOperand() == null && operation.getType() != OperatorType.DELETION) {
            getProcessBuilder().addError("operation on activity " + activity.getName() + " has no expression in right operand");
        }
        return this;
    }

    public ActivityDefinitionBuilder addLoop(final boolean testBefore, final Expression condition) {
        final StandardLoopCharacteristics loopCharacteristics = new StandardLoopCharacteristics(condition, testBefore);
        activity.setLoopCharacteristics(loopCharacteristics);
        return this;
    }

    public ActivityDefinitionBuilder addLoop(final boolean testBefore, final Expression condition, final Expression loopMax) {
        final StandardLoopCharacteristics loopCharacteristics = new StandardLoopCharacteristics(condition, testBefore, loopMax);
        activity.setLoopCharacteristics(loopCharacteristics);
        return this;
    }

    /**
     * Add a boundary event
     *
     * @param name
     *            the name of the boundary event
     * @param interrupting
     *            define whether the boundary event is interrupting or not
     * @return
     * @since 6.0
     */
    public BoundaryEventDefinitionBuilder addBoundaryEvent(final String name, final boolean interrupting) {
        return new BoundaryEventDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, interrupting);
    }

    /**
     * Add an interrupting boundary event
     *
     * @param name
     *            the name of the boundary event
     * @return
     * @since 6.0
     */
    public BoundaryEventDefinitionBuilder addBoundaryEvent(final String name) {
        return new BoundaryEventDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, true);
    }

    public MultiInstanceLoopCharacteristicsBuilder addMultiInstance(final boolean isSequential, final Expression loopCardinality) {
        return new MultiInstanceLoopCharacteristicsBuilder(getProcessBuilder(), activity, isSequential, loopCardinality);
    }

    public MultiInstanceLoopCharacteristicsBuilder addMultiInstance(final boolean isSequential, final String loopDataInput) {
        return new MultiInstanceLoopCharacteristicsBuilder(getProcessBuilder(), activity, isSequential, loopDataInput);
    }

}
