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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SExpressionBinding extends SNamedElementBinding {

    private String content;

    private String interpreter;

    protected String returnType;

    private String type;

    private final List<SExpression> dependencies = new ArrayList<SExpression>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        interpreter = attributes.get(XMLSProcessDefinition.EXPRESSION_INTERPRETER);
        returnType = attributes.get(XMLSProcessDefinition.EXPRESSION_RETURN_TYPE);
        type = attributes.get(XMLSProcessDefinition.EXPRESSION_TYPE);
    }

    @Override
    public Object getObject() {
        SExpression expression;
        try {
            expression = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance().setName(name).setContent(content).setExpressionType(type)
                    .setInterpreter(interpreter).setReturnType(returnType).setDependencies(dependencies).done();
        } catch (final SInvalidExpressionException e) {
            throw new IllegalArgumentException("Error building SExpression", e);
        }
        return expression;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.EXPRESSION_NODE;
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.EXPRESSION_NODE.equals(name)) {
            dependencies.add((SExpression) value);
        }
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLSProcessDefinition.EXPRESSION_CONTENT.equals(name)) {
            content = value;
        }
    }

}
