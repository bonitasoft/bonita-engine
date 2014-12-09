/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import java.util.List;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.impl.BusinessDataDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;

/**
 * Bonita BPM Subscription Edition specific builder for process definition.
 * It gives access to Subscription Edition specific features:
 * <ul>
 * <li>Parameters</li>
 * <li>StringIndexes</li>
 * <li>BusinessData</li>
 * </ul>
 *
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @version 6.3.5
 * @since 6.0.0
 */
public final class ProcessDefinitionBuilderExt extends ProcessDefinitionBuilder {

    @Override
    public ProcessDefinitionBuilderExt createNewInstance(final String name, final String version) {
        super.createNewInstance(name, version);
        return this;
    }

    /**
     * Add a parameter on this process.
     * 
     * @param parameterName
     *        The name of the new {@link ParameterDefinition}
     * @param type
     *        The type of the new {@link ParameterDefinition} (complete class name)
     * @return The {@link ParameterDefinitionBuilder} containing the new {@link ParameterDefinition}
     */
    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(this, process, parameterName, type);
    }

    /**
     * Set the process string index at the given position.
     * 
     * @param index
     *        The position to be set. Valid values are between 1 and 5 (inclusive)
     * @param label
     *        The label to be displayed for this position
     * @param initialValue
     *        The expression representing the initial index value
     */
    public void setStringIndex(final int index, final String label, final Expression initialValue) {
        process.setStringIndex(index, label, initialValue);
    }

    /**
     * Add a new {@link BusinessDataDefinition} on this process.
     * 
     * @param name
     *        The name of the new {@link BusinessDataDefinition}
     * @param className
     *        The complete name of class defining the new {@link BusinessDataDefinition} type
     * @param defaultValue
     *        The expression representing the default value
     * @return The {@link BusinessDataDefinitionBuilder} containing the new {@link BusinessDataDefinition}
     */
    public BusinessDataDefinitionBuilder addBusinessData(final String name, final String className, final Expression defaultValue) {
        return new BusinessDataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    protected void validateBusinessData() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        final List<BusinessDataDefinition> businessDataDefinitions = processContainer.getBusinessDataDefinitions();
        for (final BusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
            final Expression defaultValueExpression = businessDataDefinition.getDefaultValueExpression();
            if (businessDataDefinition.isMultiple() && defaultValueExpression != null && !defaultValueExpression.getReturnType().equals(List.class.getName())) {
                addError("The return type of the initial value expression of the multiple business data: '" + businessDataDefinition.getName() + "' must be "
                        + List.class.getName());
            }

            final List<ActivityDefinition> activities = processContainer.getActivities();
            for (final ActivityDefinition activity : activities) {
                final LoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
                if (loopCharacteristics instanceof MultiInstanceLoopCharacteristics) {
                    if (businessDataDefinition.getName().equals(((MultiInstanceLoopCharacteristics) loopCharacteristics).getLoopDataInputRef())
                            && !businessDataDefinition.isMultiple()) {
                        addError("The business data " + businessDataDefinition.getName() + " used in the multi instance " + activity.getName()
                                + " must be multiple");
                    }
                }
            }
        }

        for (final ActivityDefinition activity : processContainer.getActivities()) {
            final List<BusinessDataDefinition> dataDefinitions = activity.getBusinessDataDefinitions();
            if (activity.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics) {
                final MultiInstanceLoopCharacteristics multiInstanceCharacteristics = (MultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
                final String loopDataInputRef = multiInstanceCharacteristics.getLoopDataInputRef();
                if (!isReferenceValid(loopDataInputRef)) {
                    addError("The activity " + activity.getName() + "contains a reference " + loopDataInputRef
                            + " for the loop data input to an unknown data");
                }
                final String dataInputItemRef = multiInstanceCharacteristics.getDataInputItemRef();
                if (!isReferenceValid(dataInputItemRef, activity)) {
                    addError("The activity " + activity.getName() + "contains a reference " + dataInputItemRef
                            + " for the data input item to an unknown data");
                }
                final String dataOutputItemRef = multiInstanceCharacteristics.getDataOutputItemRef();
                if (!isReferenceValid(dataOutputItemRef, activity)) {
                    addError("The activity " + activity.getName() + "contains a reference " + dataOutputItemRef
                            + " for the data output item to an unknown data");
                }
                final String loopDataOutputRef = multiInstanceCharacteristics.getLoopDataOutputRef();
                if (!isReferenceValid(loopDataOutputRef)) {
                    addError("The activity " + activity.getName() + "contains a reference " + loopDataOutputRef
                            + " for the loop data input to an unknown data");
                }
            } else if (!dataDefinitions.isEmpty()) {
                addError("The activity " + activity.getName() + " contains business data but this activity does not have the multiple instance behaviour");
            }
        }
    }

    private boolean isReferenceValid(final String dataReference) {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        return dataReference == null || processContainer.getBusinessDataDefinition(dataReference) != null
                || processContainer.getDataDefinition(dataReference) != null;
    }

    private boolean isReferenceValid(final String dataReference, final ActivityDefinition activity) {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        return dataReference == null || activity.getBusinessDataDefinition(dataReference) != null
                || processContainer.getBusinessDataDefinition(dataReference) != null || activity.getDataDefinition(dataReference) != null
                || processContainer.getDataDefinition(dataReference) != null;
    }

}
