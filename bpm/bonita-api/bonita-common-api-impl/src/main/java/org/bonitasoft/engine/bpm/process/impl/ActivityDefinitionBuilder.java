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

import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristicsImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;

import java.io.Serializable;
import java.util.Date;


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

    /**
     * Sets the display description on this activity
     *
     * @param displayDescription
     *        expression representing the display description
     * @return
     */
    public ActivityDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        activity.setDisplayDescription(displayDescription);
        return this;
    }

    /**
     * Sets the display name on this activity. When set, the display name will replace the name in the Bonita BPM Portal
     *
     * @param displayName
     *        expression representing the display name
     * @return
     */
    public ActivityDefinitionBuilder addDisplayName(final Expression displayName) {
        activity.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the display description after completion on this activity. This will be used to updated the display description when the activity completes its
     * execution
     *
     * @param displayDescriptionAfterCompletion
     *        expression representing the new display description after the activity completion.
     * @return
     */
    public ActivityDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        activity.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

    /**
     * Adds an operation on this activity. Operations are executed between connectors ON_ENTER and connectors ON_FINISH. In the case of human tasks, operations
     * are executed after calling the method {@link org.bonitasoft.engine.api.ProcessAPI#executeFlowNode(long)}
     *
     * @param leftOperand
     *        operation left operand
     * @param type
     *        operator type
     * @param operator
     *        operator
     * @param rightOperand
     *        expression representing the right operand
     * @return
     */
    public ActivityDefinitionBuilder addOperation(final LeftOperand leftOperand, final OperatorType type, final String operator, final Expression rightOperand) {
        activity.addOperation(new OperationBuilder().createNewInstance().setRightOperand(rightOperand).setType(type).setOperator(operator)
                .setLeftOperand(leftOperand).done());
        if (rightOperand == null) {
            getProcessBuilder().addError("operation on activity " + activity.getName() + " has no expression in right operand");
        }
        return this;
    }

    /**
     * Adds an operation on this activity. Operations are executed between connectors ON_ENTER and connectors ON_FINISH. In the case of human tasks, operations
     * are executed after calling the method {@link org.bonitasoft.engine.api.ProcessAPI#executeFlowNode(long)}
     *
     * @param leftOperand
     *        operation left operand
     * @param type
     *        operator type
     * @param operator
     *        operator
     * @param operatorInputType
     *        the input operator type. For instance, the parameter type in the case of a Java setter
     * @param rightOperand
     *        expression representing the right operand
     * @return
     */
    public ActivityDefinitionBuilder addOperation(final LeftOperand leftOperand, final OperatorType type, final String operator,
            final String operatorInputType, final Expression rightOperand) {
        final Operation operation = new OperationBuilder().createNewInstance().setRightOperand(rightOperand).setType(type).setOperator(operator)
                .setOperatorInputType(operatorInputType).setLeftOperand(leftOperand).done();
        return addOperation(operation);
    }

    /**
     * Adds the given operation on this activity
     *
     * @param operation
     *        operation to be added
     * @return
     */
    public ActivityDefinitionBuilder addOperation(final Operation operation) {
        activity.addOperation(operation);
        checkRightOperand(operation);
        return this;
    }

    private void checkRightOperand(final Operation operation) {
        if (operation.getRightOperand() == null && operation.getType() != OperatorType.DELETION) {
            getProcessBuilder().addError("operation on activity " + activity.getName() + " has no expression in right operand");
        }
    }

    /**
     * Defines this activity as a loop. The loop will finish when the condition is evaluated to false
     *
     * @param testBefore
     *        true if the condition must be check before execute the first iteration; false if the condition must be checked only after the first iteration
     * @param condition
     *        condition determining whether the activity must loop again
     * @return
     */
    public ActivityDefinitionBuilder addLoop(final boolean testBefore, final Expression condition) {
        final StandardLoopCharacteristicsImpl loopCharacteristics = new StandardLoopCharacteristicsImpl(condition, testBefore);
        activity.setLoopCharacteristics(loopCharacteristics);
        return this;
    }

    /**
     * Defines this activity as a loop. The loop will finish when the condition is evaluated to false or when the max iterations number is reached
     *
     * @param testBefore
     *        true if the condition must be check before execute the first iteration; false if the condition must be checked only after the first iteration
     * @param condition
     *        condition determining whether the activity must loop again. The loop will finish when the condition is evaluated to false
     * @param loopMax
     *        expression representing the max iterations number. The expression must return an Integer
     * @return
     */
    public ActivityDefinitionBuilder addLoop(final boolean testBefore, final Expression condition, final Expression loopMax) {
        final StandardLoopCharacteristicsImpl loopCharacteristics = new StandardLoopCharacteristicsImpl(condition, testBefore, loopMax);
        activity.setLoopCharacteristics(loopCharacteristics);
        return this;
    }

    /**
     * Adds a boundary event
     *
     * @param name
     *        the name of the boundary event
     * @param interrupting
     *        defines whether the boundary event is interrupting or not
     * @return
     * @since 6.0
     */
    public BoundaryEventDefinitionBuilder addBoundaryEvent(final String name, final boolean interrupting) {
        return new BoundaryEventDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, interrupting);
    }

    /**
     * Adds an interrupting boundary event
     *
     * @param name
     *        the name of the boundary event
     * @return
     * @since 6.0
     */
    public BoundaryEventDefinitionBuilder addBoundaryEvent(final String name) {
        return new BoundaryEventDefinitionBuilder(getProcessBuilder(), getContainer(), activity, name, true);
    }

    /**
     * Defines this activity as a multi-instance by suppling the cardinality
     *
     * @param isSequential defines whether instances creation is sequential or not. If true, instances will be created iteration by iteration; otherwise all
     *        instances will be created during the activity initialization.
     * @param loopCardinality expression representing how many instances must be created. The expression return type must be Integer
     * @return
     */
    public MultiInstanceLoopCharacteristicsBuilder addMultiInstance(final boolean isSequential, final Expression loopCardinality) {
        return new MultiInstanceLoopCharacteristicsBuilder(getProcessBuilder(), activity, isSequential, loopCardinality);
    }

    /**
     * Defines this activity as a multi-instance by suppling a collection of elements. One instance will be created for each element in the collection.
     *
     * @param isSequential defines whether instances creation is sequential or not. If true, instances will be created iteration by iteration; otherwise all
     *        instances will be created during the activity initialization.
     * @param loopDataInput name of process data representing the collection of elements used to create the instances
     * @return
     * @see MultiInstanceLoopCharacteristicsBuilder#addLoopDataOutputRef(String)
     * @see MultiInstanceLoopCharacteristicsBuilder#addDataInputItemRef(String)
     * @see MultiInstanceLoopCharacteristicsBuilder#addDataOutputItemRef(String)
     */
    public MultiInstanceLoopCharacteristicsBuilder addMultiInstance(final boolean isSequential, final String loopDataInput) {
        return new MultiInstanceLoopCharacteristicsBuilder(getProcessBuilder(), activity, isSequential, loopDataInput);
    }

    /**
     * Adds a Business Data on the activity. The activity must contain a {@link org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics} using dataInput or dataOutput.
     *
     * @param name the name of the business data
     * @param className complete name of class defining the Business Data Type
     * @return
     */
    public ActivityDefinitionBuilder addBusinessData(final String name, final String className) {
        final BusinessDataDefinitionImpl businessData = new BusinessDataDefinitionImpl(name, null);
        businessData.setClassName(className);
        activity.addBusinessDataDefinition(businessData);
        return this;
    }

}
