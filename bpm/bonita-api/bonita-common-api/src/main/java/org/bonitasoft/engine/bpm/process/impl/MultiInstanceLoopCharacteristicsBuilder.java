/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.flownode.impl.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.MultiInstanceLoopCharacteristics;
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

    public MultiInstanceLoopCharacteristicsBuilder addCompletionCondition(final Expression completionCondition) {
        entity.setCompletionCondition(completionCondition);
        if (!Boolean.class.getName().equals(completionCondition.getReturnType())) {
            builder.addError("the completion condition of the looped activity " + activityDefinition.getName() + " do not have the return type \"Boolean\"");
        }
        return this;
    }

    public MultiInstanceLoopCharacteristicsBuilder addLoopDataOutputRef(final String loopDataOutputRef) {
        entity.setLoopDataOutputRef(loopDataOutputRef);
        return this;
    }

    public MultiInstanceLoopCharacteristicsBuilder addDataInputItemRef(final String dataItemRef) {
        entity.setDataInputItemRef(dataItemRef);
        return this;
    }

    public MultiInstanceLoopCharacteristicsBuilder addDataOutputItemRef(final String dataItemRef) {
        entity.setDataOutputItemRef(dataItemRef);
        return this;
    }

}
