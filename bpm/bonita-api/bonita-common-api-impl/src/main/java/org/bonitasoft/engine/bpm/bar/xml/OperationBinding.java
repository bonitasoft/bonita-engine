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

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.io.xml.ElementBinding;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.operation.impl.OperationImpl;

import java.util.Map;


/**
 * @author Elias Ricken de Medeiros
 */
public class OperationBinding extends ElementBinding {

    private Expression rightOperand;

    private LeftOperand leftOperand;

    private String operator;

    private OperatorType operatorType;

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.OPERATION_RIGHT_OPERAND.equals(name)) {
            rightOperand = (Expression) value;
        }
        if (XMLProcessDefinition.OPERATION_LEFT_OPERAND.equals(name)) {
            leftOperand = (LeftOperand) value;
        }
    }

    @Override
    public Object getObject() {
        final OperationImpl operation = new OperationImpl();
        operation.setOperator(operator);
        operation.setRightOperand(rightOperand);
        operation.setType(operatorType);
        operation.setLeftOperand(leftOperand);
        return operation;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.OPERATION_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {

    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        operator = attributes.get(XMLProcessDefinition.OPERATION_OPERATOR);
        operatorType = OperatorType.valueOf(attributes.get(XMLProcessDefinition.OPERATION_OPERATOR_TYPE));
    }

}
