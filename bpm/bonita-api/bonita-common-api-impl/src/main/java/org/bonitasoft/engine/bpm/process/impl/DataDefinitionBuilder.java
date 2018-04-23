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

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class DataDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final DataDefinitionImpl dataDefinition;

    protected DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final DataDefinitionImpl dataDefinition) {
        super(container, processDefinitionBuilder);
        this.dataDefinition = dataDefinition;
        container.addDataDefinition(dataDefinition);
    }

    protected DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final ActivityDefinitionImpl activity, final DataDefinitionImpl dataDefinition) {
        super(container, processDefinitionBuilder);
        this.dataDefinition = dataDefinition;
        activity.addDataDefinition(dataDefinition);
    }

    protected DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final ThrowMessageEventTriggerDefinitionImpl messageEventTrigger, final DataDefinitionImpl dataDefinition) {
        super(container, processDefinitionBuilder);
        this.dataDefinition = dataDefinition;
        messageEventTrigger.addDataDefinition(dataDefinition);
    }

    protected DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final CatchMessageEventTriggerDefinitionImpl messageEventTrigger, final DataDefinitionImpl dataDefinition) {
        super(container, processDefinitionBuilder);
        this.dataDefinition = dataDefinition;
    }

    private static DataDefinitionImpl getData(final String name, final String className, final Expression defaultValue) {
        final DataDefinitionImpl data = new DataDefinitionImpl(name, defaultValue);
        data.setClassName(className);
        return data;
    }

    public DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String name, final String className, final Expression defaultValue) {
        this(processDefinitionBuilder, container, getData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
        processDefinitionBuilder.checkName(dataDefinition.getName());
    }

    public DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final ActivityDefinitionImpl activity, final String name, final String className, final Expression defaultValue) {
        this(processDefinitionBuilder, container, activity, getData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
        processDefinitionBuilder.checkName(dataDefinition.getName());
    }

    public DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final ThrowMessageEventTriggerDefinitionImpl messageEventTrigger, final String name, final String className, final Expression defaultValue) {
        this(processDefinitionBuilder, container, messageEventTrigger, getData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
        processDefinitionBuilder.checkName(dataDefinition.getName());
    }

    public DataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final CatchMessageEventTriggerDefinitionImpl messageEventTrigger, final String name, final String className, final Expression defaultValue) {
        this(processDefinitionBuilder, container, messageEventTrigger, getData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
        processDefinitionBuilder.checkName(dataDefinition.getName());
    }

    @Override
    public DataDefinitionBuilder addDescription(final String description) {
        dataDefinition.setDescription(description);
        return this;
    }

    /**
     * Marks this data as transient
     * 
     * @return
     */
    public DataDefinitionBuilder isTransient() {
        dataDefinition.setTransientData(true);
        return this;
    }

    protected DataDefinition getDataDefinition() {
        return dataDefinition;
    }

}
