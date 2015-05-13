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

import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Matthieu Chaffotte
 */
public class ConstraintDefinitionBinding extends NamedElementBinding {

    private String expression;

    private String explanation;

    private final List<String> inputNames;

    public ConstraintDefinitionBinding() {
        inputNames = new ArrayList<String>();
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
        if (XMLProcessDefinition.CONSTRAINT_EXPRESSION.equals(name)) {
            expression = value;
        } else if (XMLProcessDefinition.CONSTRAINT_EXPLANATION.equals(name)) {
            explanation = value;
        } else if (XMLProcessDefinition.INPUT_NAME.equals(name)) {
            inputNames.add(value);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {
    }

    @Override
    public Object getObject() {
        final ConstraintDefinitionImpl rule = new ConstraintDefinitionImpl(name, expression, explanation);
        for (final String inputName : inputNames) {
            rule.addInputName(inputName);
        }
        return rule;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CONTRACT_CONSTRAINT_NODE;
    }

}
