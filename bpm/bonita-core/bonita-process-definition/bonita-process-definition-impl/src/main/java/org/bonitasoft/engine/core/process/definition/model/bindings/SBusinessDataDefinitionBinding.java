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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.SBusinessDataDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.SBusinessDataDefinitionBuilderFactory;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SBusinessDataDefinitionBinding extends SNamedElementBinding {

    protected String description;

    protected SExpression defaultValue;

    protected String className;

    protected boolean multiple;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        className = attributes.get(XMLSProcessDefinition.BUSINESS_DATA_DEFINITION_CLASS);
        multiple = Boolean.valueOf(attributes.get(XMLSProcessDefinition.BUSINESS_DATA_DEFINITION_IS_MULTIPLE));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLSProcessDefinition.DESCRIPTION.equals(name)) {
            description = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.DEFAULT_VALUE_NODE.equals(name)) {
            defaultValue = (SExpression) value;
        }
    }

    @Override
    public SBusinessDataDefinition getObject() {
        final SBusinessDataDefinitionBuilder businessDataDefinitionImpl = BuilderFactory.get(SBusinessDataDefinitionBuilderFactory.class).createNewInstance(
                name, className);
        if (description != null) {
            businessDataDefinitionImpl.setDescription(description);
        }
        businessDataDefinitionImpl.setDefaultValue(defaultValue);
        businessDataDefinitionImpl.setMultiple(multiple);
        return businessDataDefinitionImpl.done();
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.BUSINESS_DATA_DEFINITION_NODE;
    }

}
