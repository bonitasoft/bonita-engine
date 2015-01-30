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
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilderFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SXMLDataDefinitionBinding extends SDataDefinitionBinding {

    private String namespace;

    private String element;

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        super.setChildElement(name, value, attributes);
        if (XMLSProcessDefinition.XML_DATA_DEFINITION_NAMESPACE.equals(name)) {
            namespace = value;
        }
        if (XMLSProcessDefinition.XML_DATA_DEFINITION_ELEMENT.equals(name)) {
            element = value;
        }
    }

    @Override
    public SDataDefinition getObject() {
        final SXMLDataDefinitionBuilder dataDefinitionImpl = BuilderFactory.get(SXMLDataDefinitionBuilderFactory.class).createNewXMLData(name);
        if (description != null) {
            dataDefinitionImpl.setDescription(description);
        }
        dataDefinitionImpl.setDefaultValue(defaultValue);
        dataDefinitionImpl.setTransient(Boolean.valueOf(isTransient));
        dataDefinitionImpl.setNamespace(namespace);
        dataDefinitionImpl.setElement(element);
        return dataDefinitionImpl.done();
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.XML_DATA_DEFINITION_NODE;
    }

}
