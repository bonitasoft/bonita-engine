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

import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class MultiInstanceLoopCharacteristicsBuilder {

    private final MultiInstanceLoopCharacteristics entity;

    private final ProcessDefinitionBuilder builder;

    private final ActivityDefinitionImpl activityDefinition;

    public MultiInstanceLoopCharacteristicsBuilder(final ProcessDefinitionBuilder builder, final ActivityDefinitionImpl activityDefinition,
            final boolean isSequential, final Expression loopCardinality) {
        this.activityDefinition = activityDefinition;
        this.builder = builder;
        entity = new MultiInstanceLoopCharacteristics(isSequential, loopCardinality);
        activityDefinition.setLoopCharacteristics(entity);
        if (!Integer.class.getName().equals(loopCardinality.getReturnType())) {
            builder.addError("the loop cardinality of the looped activity " + activityDefinition.getName() + " do not have the return type \"Integer\"");
        }
    }

    public MultiInstanceLoopCharacteristicsBuilder(final ProcessDefinitionBuilder builder, final ActivityDefinitionImpl activityDefinition,
            final boolean isSequential, final String loopDataInputRef) {
        this.activityDefinition = activityDefinition;
        this.builder = builder;
        entity = new MultiInstanceLoopCharacteristics(isSequential, loopDataInputRef);
        activityDefinition.setLoopCharacteristics(entity);
    }

    /**
     * Adds a condition completion on this multi-instance
     * 
     * @param completionCondition
     *            expression used as completion condition. The return type must be boolean
     * @return
     */
    public MultiInstanceLoopCharacteristicsBuilder addCompletionCondition(final Expression completionCondition) {
        entity.setCompletionCondition(completionCondition);
        if (!Boolean.class.getName().equals(completionCondition.getReturnType())) {
            builder.addError("the completion condition of the looped activity " + activityDefinition.getName() + " do not have the return type \"Boolean\"");
        }
        return this;
    }

    /**
     * Adds a data output reference. A data output reference must be used when the instance is initialized using a collection:
     * {@link ActivityDefinitionBuilder#addMultiInstance(boolean, String)}. It represents the output generated for all instances:
     * {@link MultiInstanceLoopCharacteristicsBuilder#addDataOutputItemRef(String)}
     * 
     * @param loopDataOutputRef
     *            name of process data that will store the collection output. This data must be previously added at the process level.
     * @return
     * @see MultiInstanceLoopCharacteristicsBuilder#addDataOutputItemRef(String)
     */
    public MultiInstanceLoopCharacteristicsBuilder addLoopDataOutputRef(final String loopDataOutputRef) {
        entity.setLoopDataOutputRef(loopDataOutputRef);
        return this;
    }

    /**
     * Adds a data input item reference on this multi-instance. A data input item reference must be used when the instance is initialized using a collection:
     * {@link ActivityDefinitionBuilder#addMultiInstance(boolean, String)}. It represents the collection element related to each instance. For instance, if the
     * collection used to create the multi-instance is a list containing the elements A and B, in the first instance the data input item reference value will be
     * A and in the second one, B.
     * 
     * @param dataItemRef
     *            name of activity data that will store the collection element related to each instance. This data must be previously added at the activity
     *            level.
     * @return
     */
    public MultiInstanceLoopCharacteristicsBuilder addDataInputItemRef(final String dataItemRef) {
        entity.setDataInputItemRef(dataItemRef);
        return this;
    }

    /**
     * Adds a data output item reference on this multi-instance. A data output item reference must be used when the instance is initialized using a collection:
     * {@link ActivityDefinitionBuilder#addMultiInstance(boolean, String)}. It represents the output generated for each instance. All data output item reference
     * will be added in the data output reference: {@link MultiInstanceLoopCharacteristicsBuilder#addLoopDataOutputRef(String)}.
     * 
     * @param dataItemRef
     *            name of activity data that will store the output related each instance. This data must be previously added at the activity level.
     * @return
     */
    public MultiInstanceLoopCharacteristicsBuilder addDataOutputItemRef(final String dataItemRef) {
        entity.setDataOutputItemRef(dataItemRef);
        return this;
    }

}
