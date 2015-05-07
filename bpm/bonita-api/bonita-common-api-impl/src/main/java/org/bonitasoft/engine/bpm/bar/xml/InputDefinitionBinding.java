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
 */
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Matthieu Chaffotte
 */
public class InputDefinitionBinding extends NamedElementBinding {

    private final List<InputDefinition> inputDefinitionList = new ArrayList<>();
    protected boolean multiple;
    private Type type;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        multiple = Boolean.parseBoolean(attributes.get(XMLProcessDefinition.MULTIPLE));
        String typeAsString = attributes.get(XMLProcessDefinition.TYPE);
        if (typeAsString != null) {
            type = Type.valueOf(typeAsString);
        }
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {
        if (name.equals(XMLProcessDefinition.CONTRACT_INPUT_NODE)) {
            inputDefinitionList.add((InputDefinition) value);
        }
    }

    @Override
    public Object getObject() {
        return new InputDefinitionImpl(name, description, multiple, type, inputDefinitionList);
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CONTRACT_INPUT_NODE;
    }

}
