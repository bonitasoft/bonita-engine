/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.impl.OperationImpl;

/**
 * Utilitary builder to creation <code>Operation</code> objects. <code>Operation</code>s are a way to 'assign', 'operate', 'set a new value', ... on something.
 * See {@link OperatorType} for the different types of operation.
 * 
 * @see OperatorType
 * @see Operation
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class OperationBuilder {

    private OperationImpl operation;

    /**
     * Initiate the building of a new <code>Operation</code>. The <code>Operation</code> building will be complete when calling the {@link #done()} method.
     * 
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder createNewInstance() {
        operation = new OperationImpl();
        return this;
    }

    /**
     * Sets the <code>LeftOperand</code> of this operation. A <code>LeftOperand</code> can be obtained by using <code>LeftOperandBuilder</code>.
     * 
     * @param leftOperand
     *            the <code>LeftOperand</code> to set.
     * @return this builder itself, so that calls the various exposed methods can be chained.
     * @see LeftOperandBuilder
     */
    public OperationBuilder setLeftOperand(final LeftOperand leftOperand) {
        operation.setLeftOperand(leftOperand);
        return this;
    }

    /**
     * Sets the <code>LeftOperand</code> of this operation. It is built for you with its name and external properties.
     * 
     * @param name
     *            the name of the left operand
     * @param external
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setLeftOperand(final String name, final boolean external) {
        operation.setLeftOperand(new LeftOperandBuilder().createNewInstance(name).setExternal(external).done());
        return this;
    }

    /**
     * Sets the <code>LeftOperand</code> of this operation. It is built for you with its name and external properties.
     * 
     * @param name
     *            the name of the left operand
     * @param external
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setLeftOperand(final String name, final LeftOperandType type, final boolean external) {
        operation.setLeftOperand(new LeftOperandBuilder().createNewInstance(name).setType(type).setExternal(external).done());
        return this;
    }

    /**
     * @param operatorType
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setType(final OperatorType operatorType) {
        operation.setType(operatorType);
        return this;
    }

    /**
     * @param operator
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setOperator(final String operator) {
        operation.setOperator(operator);
        return this;
    }

    /**
     * @param operatorInputType
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setOperatorInputType(final String operatorInputType) {
        operation.setOperatorInputType(operatorInputType);
        return this;
    }

    /**
     * @param rightOperand
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setRightOperand(final Expression rightOperand) {
        operation.setRightOperand(rightOperand);
        return this;
    }

    /**
     * @param dataName
     * @param expression
     * @return the newly created <code>Operation</code>.
     */
    public Operation createSetDataOperation(final String dataName, final Expression expression) {
        return createNewInstance().setLeftOperand(dataName, LeftOperandType.DATA, false).setRightOperand(expression).setType(OperatorType.ASSIGNMENT).done();
    }

    /**
     * @param docName
     * @param expression
     * @return the newly created <code>Operation</code>.
     */
    public Operation createSetDocument(final String docName, final Expression expression) {
        return createNewInstance().setLeftOperand(docName, LeftOperandType.DOCUMENT, false).setType(OperatorType.ASSIGNMENT).setRightOperand(expression).done();
    }

    /**
     * @param xmlName
     * @param xPath
     * @param setValue
     * @return the newly created <code>Operation</code>.
     */
    public Operation createXPathOperation(final String xmlName, final String xPath, final Expression setValue) {
        return createNewInstance().setLeftOperand(xmlName, LeftOperandType.DATA, false).setType(OperatorType.XPATH_UPDATE_QUERY).setOperator(xPath)
                .setRightOperand(setValue).done();
    }

    /**
     * @param objectName
     * @param methodName
     * @param methodParamType
     * @param methodParams
     * @return the newly created <code>Operation</code>.
     */
    public Operation createJavaMethodOperation(final String objectName, final String methodName, final String methodParamType, final Expression methodParams) {
        return createNewInstance().setLeftOperand(objectName, LeftOperandType.DATA, false).setType(OperatorType.JAVA_METHOD).setOperator(methodName)
                .setOperatorInputType(methodParamType).setRightOperand(methodParams).done();
    }

    /**
     * Creates a new operation that sets a new value to a String search index.
     * 
     * @param index
     *            the search index to set
     * @param setValue
     *            the Expression to set the search index to.
     * @return the newly created <code>Operation</code>.
     */
    public Operation createSetStringIndexOperation(final int index, final Expression setValue) {
        return createNewInstance().setLeftOperand(new LeftOperandBuilder().createSearchIndexLeftOperand(index)).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(setValue).done();
    }

    /**
     * @return the newly built <code>Operation</code>.
     */
    public Operation done() {
        if (operation.getType() == null) {
            throw new IllegalStateException("The type of the expression is not set");
        }
        return operation;
    }

    /* =================== Fluent api ======================= */

    public static OperationRightOperandBuilder setData(final String name) {
        return new OperationRightOperandBuilder(new OperationBuilder().createNewInstance().setLeftOperand(name, LeftOperandType.DATA, false));
    }

    public static OperationRightOperandBuilder setStringIndex(final int index) {
        return new OperationRightOperandBuilder(new OperationBuilder().createNewInstance().setLeftOperand(
                new LeftOperandBuilder().createSearchIndexLeftOperand(index)));

    }

    public static OperationRightOperandBuilder setDocument(final String name) {
        return new OperationRightOperandBuilder(new OperationBuilder().createNewInstance().setLeftOperand(name, LeftOperandType.DOCUMENT, false));

    }

    // OperationRightOperandBuilder setTransientData(final String name) {
    //
    // }

    OperationRightOperandBuilder setExternalData(final String name) {
        return new OperationRightOperandBuilder(new OperationBuilder().createNewInstance().setLeftOperand(name, LeftOperandType.DATA, true));

    }

}
