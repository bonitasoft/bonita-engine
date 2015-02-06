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

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Emmanuel Duchastenier
 */
public class BusinessDataDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final BusinessDataDefinitionImpl businessDataDefinition;

    protected BusinessDataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final BusinessDataDefinitionImpl businessDataDefinition) {
        super(container, processDefinitionBuilder);
        this.businessDataDefinition = businessDataDefinition;
        container.addBusinessDataDefinition(businessDataDefinition);
    }

    private static BusinessDataDefinitionImpl getBusinessData(final String name, final String className, final Expression defaultValue) {
        final BusinessDataDefinitionImpl businessData = new BusinessDataDefinitionImpl(name, defaultValue);
        businessData.setClassName(className);
        return businessData;
    }

    public BusinessDataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String name, final String className, final Expression defaultValue) {
        this(processDefinitionBuilder, container, getBusinessData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
        processDefinitionBuilder.checkName(businessDataDefinition.getName());
    }

    public BusinessDataDefinitionBuilder setMultiple(final boolean isMultiple) {
        businessDataDefinition.setMultiple(isMultiple);
        return this;
    }

    @Override
    public BusinessDataDefinitionBuilder addDescription(final String description) {
        businessDataDefinition.setDescription(description);
        return this;
    }

    protected BusinessDataDefinition getBusinessDataDefinition() {
        return businessDataDefinition;
    }

}
