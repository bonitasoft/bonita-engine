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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class DataDefinitionBinding extends NamedElementBinding {

    protected String description;

    protected Expression defaultValue;

    protected String className;

    protected String isTransient;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        className = attributes.get(XMLProcessDefinition.DATA_DEFINITION_CLASS);
        isTransient = attributes.get(XMLProcessDefinition.DATA_DEFINITION_TRANSIENT);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLProcessDefinition.DESCRIPTION.equals(name)) {
            description = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.DEFAULT_VALUE_NODE.equals(name)) {
            defaultValue = (Expression) value;
        }
    }

    @Override
    public DataDefinitionImpl getObject() {
        final DataDefinitionImpl dataDefinitionImpl = new DataDefinitionImpl(name, defaultValue);
        if (description != null) {
            dataDefinitionImpl.setDescription(description);
        }
        dataDefinitionImpl.setClassName(className);
        dataDefinitionImpl.setTransientData(Boolean.valueOf(isTransient));
        return dataDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.DATA_DEFINITION_NODE;
    }

}
