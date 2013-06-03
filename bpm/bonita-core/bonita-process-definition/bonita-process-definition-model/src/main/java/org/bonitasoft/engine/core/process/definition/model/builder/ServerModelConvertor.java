/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.TextDataDefinition;
import org.bonitasoft.engine.bpm.data.XMLDataDefinition;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Baptiste Mesta
 */
public class ServerModelConvertor {

    public static SExpression convertExpression(final SExpressionBuilders sExpressionBuilders, final Expression value) {
        if (value == null) {
            return null;
        } else {
            final ArrayList<SExpression> dependencies = new ArrayList<SExpression>();
            for (final Expression expression : value.getDependencies()) {
                dependencies.add(convertExpression(sExpressionBuilders, expression));
            }
            final SExpressionBuilder expressionBuilder = sExpressionBuilders.getExpressionBuilder();
            try {
                return expressionBuilder.createNewInstance().setName(value.getName()).setContent(value.getContent())
                        .setExpressionType(value.getExpressionType()).setInterpreter(value.getInterpreter()).setReturnType(value.getReturnType())
                        .setDependencies(dependencies).done();
            } catch (final SInvalidExpressionException e) {
                throw new IllegalArgumentException("Error building SExpression", e);
            }
        }
    }

    public static SOperation convertOperation(final SOperationBuilders sOperationBuilders, final SExpressionBuilders sExpressionBuilders,
            final Operation operation) {
        return sOperationBuilders.getSOperationBuilder().createNewInstance().setOperator(operation.getOperator())
                .setType(SOperatorType.valueOf(operation.getType().name()))
                .setRightOperand(ServerModelConvertor.convertExpression(sExpressionBuilders, operation.getRightOperand()))
                .setLeftOperand(sOperationBuilders.getSLeftOperandBuilder().createNewInstance().setName(operation.getLeftOperand().getName()).done()).done();
    }

    public static List<SOperation> convertOperations(final SOperationBuilders sOperationBuilders, final SExpressionBuilders sExpressionBuilders,
            final List<Operation> operations) {
        final List<SOperation> sOperations = new ArrayList<SOperation>(operations.size());
        for (final Operation operation : operations) {
            sOperations.add(convertOperation(sOperationBuilders, sExpressionBuilders, operation));
        }
        return sOperations;
    }

    /**
     * @param dataDefinition
     * @param sDataDefinitionBuilders
     * @param sExpressionBuilders
     * @return
     */
    public static SDataDefinition convertDataDefinition(final DataDefinition dataDefinition, final SDataDefinitionBuilders sDataDefinitionBuilders,
            final SExpressionBuilders sExpressionBuilders) {
        if (dataDefinition instanceof XMLDataDefinition) {
            final XMLDataDefinition xmlDataDef = (XMLDataDefinition) dataDefinition;
            final SXMLDataDefinitionBuilder xmlDataDefinitionBuilder = sDataDefinitionBuilders.getXMLDataDefinitionBuilder();
            xmlDataDefinitionBuilder.createNewXMLData(dataDefinition.getName()).setElement(xmlDataDef.getElement()).setNamespace(xmlDataDef.getNamespace());
            xmlDataDefinitionBuilder.setDefaultValue(ServerModelConvertor.convertExpression(sExpressionBuilders, dataDefinition.getDefaultValueExpression()));
            xmlDataDefinitionBuilder.setDescription(dataDefinition.getDescription());
            xmlDataDefinitionBuilder.setTransient(dataDefinition.isTransientData());
            return xmlDataDefinitionBuilder.done();
        } else {
            final SDataDefinitionBuilder dataDefinitionBuilder = sDataDefinitionBuilders.getDataDefinitionBuilder();
            if (dataDefinition instanceof TextDataDefinition) {
                final TextDataDefinition textDataDefinition = (TextDataDefinition) dataDefinition;
                dataDefinitionBuilder.createNewTextData(dataDefinition.getName()).setAsLongText(textDataDefinition.isLongText());
            } else {
                dataDefinitionBuilder.createNewInstance(dataDefinition.getName(), dataDefinition.getClassName());
            }
            dataDefinitionBuilder.setDefaultValue(ServerModelConvertor.convertExpression(sExpressionBuilders, dataDefinition.getDefaultValueExpression()));
            dataDefinitionBuilder.setDescription(dataDefinition.getDescription());
            dataDefinitionBuilder.setTransient(dataDefinition.isTransientData());
            return dataDefinitionBuilder.done();
        }
    }
}
