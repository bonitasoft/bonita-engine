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
 * @author Matthieu Chaffotte
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
     *        the <code>LeftOperand</code> to set.
     * @return this builder itself, so that calls the various exposed methods can be chained.
     * @see LeftOperandBuilder
     */
    public OperationBuilder setLeftOperand(final LeftOperand leftOperand) {
        operation.setLeftOperand(leftOperand);
        return this;
    }

    /**
     * @deprecated use setLeftOperand(String,String)
     *             Sets the <code>LeftOperand</code> of this operation. It is built for you with its name and external properties.
     * @param name
     *        the name of the left operand
     * @param external
     *        is the data managed externally and thus should not be tried to be updated?
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    @Deprecated
    public OperationBuilder setLeftOperand(final String name, final boolean external) {
        operation.setLeftOperand(new LeftOperandBuilder().createNewInstance(name).setExternal(external).done());
        return this;
    }

    /**
     * @deprecated use setLeftOperand(String,String)
     *             Sets the <code>LeftOperand</code> of this operation. It is built for you with its name and external properties.
     * @param name
     *        the name of the left operand
     * @param type
     * @param external
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    @Deprecated
    public OperationBuilder setLeftOperand(final String name, LeftOperandType type, final boolean external) {
        if (type == LeftOperandType.DATA && external) {
            type = LeftOperandType.EXTERNAL_DATA;
        }
        operation.setLeftOperand(new LeftOperandBuilder().createNewInstance(name).setType(type.name()).done());
        return this;
    }

    /**
     * Sets the <code>LeftOperand</code> of this operation. It is built for you with its name and external properties.
     *
     * @param name
     *        the name of the left operand
     * @param type
     * @return this builder itself, so that calls the various exposed methods can be chained.
     */
    public OperationBuilder setLeftOperand(final String name, final String type) {
        operation.setLeftOperand(new LeftOperandBuilder().createNewInstance(name).setType(type).done());
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
        return createNewInstance().setLeftOperand(dataName, LeftOperand.TYPE_DATA).setRightOperand(expression).setType(OperatorType.ASSIGNMENT).done();
    }

    /**
     * Creates a new operation of type {@link org.bonitasoft.engine.operation.LeftOperand#TYPE_BUSINESS_DATA} that allows to update a Business Data by calling a Java setter on one of
     * its attributes.
     *
     * @param businessDataName
     *        the name of the business data to update.
     * @param methodName
     *        the Java setter method to call.
     * @param methodParamType
     *        the type of the Java setter method parameter (to be able to differentiate 2 methods with the same name but with different parameter types)
     * @param expression
     *        the Expression to evaluate that represents the new value to set.
     * @return the newly created <code>Operation</code>.
     */
    public Operation createBusinessDataSetAttributeOperation(final String businessDataName, final String methodName, final String methodParamType,
            final Expression expression) {
        return createNewInstance().setLeftOperand(new LeftOperandBuilder().createBusinessDataLeftOperand(businessDataName)).setRightOperand(expression)
                .setType(OperatorType.JAVA_METHOD).setOperator(methodName).setOperatorInputType(methodParamType).done();
    }

    /**
     * Creates a new operation of type {@link OperatorType#ASSIGNMENT} that associates an existing Business Data to the current process.
     *
     * @param businessDataName
     *        the name of the reference in the process.
     * @param expressionReturningBusinessData
     *        the expression returning an existing business data.
     * @return the newly created <code>Operation</code>.
     * @see org.bonitasoft.engine.operation.LeftOperand#TYPE_BUSINESS_DATA
     */
    public Operation attachBusinessDataSetAttributeOperation(final String businessDataName, final Expression expressionReturningBusinessData) {
        return createNewInstance().setLeftOperand(new LeftOperandBuilder().createBusinessDataLeftOperand(businessDataName))
                .setRightOperand(expressionReturningBusinessData).setType(OperatorType.ASSIGNMENT).done();
    }

    /**
     * Creates a new operation of type {@link OperatorType#ASSIGNMENT} that remove the named Business Data of the current process.
     *
     * @param businessDataName
     *        the name of the reference in the process.
     * @return the newly created <code>Operation</code>.
     * @see OperatorType#DELETION
     */
    public Operation deleteBusinessDataOperation(final String businessDataName) {
        return createNewInstance().setLeftOperand(new LeftOperandBuilder().createBusinessDataLeftOperand(businessDataName)).setType(OperatorType.DELETION)
                .done();
    }

    /**
     * create an operation that update a document
     *
     * @param docName
     *        the name of the document
     * @param expression
     *        the expression that returns a {@link org.bonitasoft.engine.bpm.document.DocumentValue}
     * @return the newly created <code>Operation</code>.
     */
    public Operation createSetDocument(final String docName, final Expression expression) {
        return createNewInstance().setLeftOperand(docName, LeftOperand.TYPE_DOCUMENT).setType(OperatorType.ASSIGNMENT).setRightOperand(expression).done();
    }

    /**
     * create an operation that update a document list
     *
     * @param docName
     *        the name of the document list
     * @param expression
     *        the expression that returns a list of {@link org.bonitasoft.engine.bpm.document.DocumentValue}
     * @return the newly created <code>Operation</code>.
     */
    public Operation createSetDocumentList(final String docName, final Expression expression) {
        return createNewInstance().setLeftOperand(docName, LeftOperand.TYPE_DOCUMENT_LIST).setType(OperatorType.ASSIGNMENT).setRightOperand(expression).done();
    }

    /**
     * create an operation that update an xml data using a xpath expression
     *
     * @param xmlName
     *        name of the data
     * @param xPath
     *        the xpath expression
     * @param setValue
     *        the value to set the node in the data with
     * @return the newly created <code>Operation</code>.
     */
    public Operation createXPathOperation(final String xmlName, final String xPath, final Expression setValue) {
        return createNewInstance().setLeftOperand(xmlName, LeftOperand.TYPE_DATA).setType(OperatorType.XPATH_UPDATE_QUERY).setOperator(xPath)
                .setRightOperand(setValue).done();
    }

    /**
     * create an operation that update a data that contains a java object
     *
     * @param objectName
     *        the name of the data
     * @param methodName
     *        the method to call on this data to update it
     * @param methodParamType
     *        the type of the parameter of the method
     * @param methodParams
     *        the value to call the method with
     * @return the newly created <code>Operation</code>.
     */
    public Operation createJavaMethodOperation(final String objectName, final String methodName, final String methodParamType, final Expression methodParams) {
        return createNewInstance().setLeftOperand(objectName, LeftOperand.TYPE_DATA).setType(OperatorType.JAVA_METHOD).setOperator(methodName)
                .setOperatorInputType(methodParamType).setRightOperand(methodParams).done();
    }

    /**
     * Creates a new operation that sets a new value to a String search index.
     *
     * @param index
     *        the search index to set
     * @param setValue
     *        the Expression to set the search index to.
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

}
