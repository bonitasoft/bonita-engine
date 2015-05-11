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

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

/**
 * @author Baptiste Mesta
 */
public class ExpressionBinding extends NamedElementBinding {

    private String content;

    private String interpreter;

    private String returnType;

    private String type;

    private final List<Expression> dependencies = new ArrayList<Expression>();

    public static final String DISPLAY_NAME = "displayName";

    public static final String DISPLAY_DESCRIPTION = "displayDescription";

    public static final String DISPLAY_DESCRIPTION_AFTER_COMPLETION = "displayDescriptionAfterCompletion";

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        interpreter = attributes.get(XMLProcessDefinition.EXPRESSION_INTERPRETER);
        returnType = attributes.get(XMLProcessDefinition.EXPRESSION_RETURN_TYPE);
        type = attributes.get(XMLProcessDefinition.EXPRESSION_TYPE);
    }

    @Override
    public Object getObject() {
        final ExpressionImpl expressionImpl = new ExpressionImpl(id);
        expressionImpl.setName(name);
        expressionImpl.setContent(content);
        expressionImpl.setExpressionType(type);
        expressionImpl.setReturnType(returnType);
        expressionImpl.setInterpreter(interpreter);
        expressionImpl.setDependencies(dependencies);
        return expressionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.EXPRESSION_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLProcessDefinition.EXPRESSION_CONTENT.equals(name)) {
            content = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.EXPRESSION_NODE.equals(name)) {
            dependencies.add((Expression) value);
        }
    }

}
