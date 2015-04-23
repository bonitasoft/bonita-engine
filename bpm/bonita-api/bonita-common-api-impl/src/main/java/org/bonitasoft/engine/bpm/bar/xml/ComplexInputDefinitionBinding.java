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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Matthieu Chaffotte
 */
public class ComplexInputDefinitionBinding extends InputDefinitionBinding {
    List<SimpleInputDefinition> simpleInputDefinitionList = new ArrayList<>();
    List<ComplexInputDefinition> complexInputDefinitionList = new ArrayList<>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {
        if(name.equals(XMLProcessDefinition.CONTRACT_COMPLEX_INPUT_NODE)){
            complexInputDefinitionList.add((ComplexInputDefinition) value);
        }
        if(name.equals(XMLProcessDefinition.CONTRACT_SIMPLE_INPUT_NODE)){
            simpleInputDefinitionList.add((SimpleInputDefinition) value);

        }
    }

    @Override
    public Object getObject() {
        return new ComplexInputDefinitionImpl(name, description, multiple, simpleInputDefinitionList, complexInputDefinitionList);
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CONTRACT_COMPLEX_INPUT_NODE;
    }

}
