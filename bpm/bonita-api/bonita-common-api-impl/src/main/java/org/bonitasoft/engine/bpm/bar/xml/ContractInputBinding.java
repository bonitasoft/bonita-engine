/*
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Map;

import org.bonitasoft.engine.expression.Expression;

/**
 * Represents a CallActivity start contract input mapping
 * author Emmanuel Duchastenier
 */
public class ContractInputBinding extends NamedElementBinding {

    /**
     * The expression in the calling process to evaluate to set the called process start contract input
     */
    private Expression expression;

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.EXPRESSION_NODE.equals(name)) {
            expression = (Expression) value;
        }
    }

    @Override
    public Object getObject() {
        return new XMLProcessDefinition.BEntry<>(name, expression);
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CONTRACT_INPUT_EXPRESSION_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }
}
