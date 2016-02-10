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
package org.bonitasoft.engine.expression;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * Allow to evaluate one kind of expression
 * the kind of expression that this evaluator is responsible for is define by the {@link #getExpressionKind()} Client implements this interface in order to add
 * a new kind of expression
 *
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ExpressionExecutorStrategy {

    String DEFINITION_ID = "processDefinitionId";// hum should not be process here

    String DEFINITION_TYPE = "PROCESS";

    String CONTAINER_ID_KEY = "containerId";

    String CONTAINER_TYPE_KEY = "containerType";

    String INTERPRETER_GROOVY = "GROOVY";

    String TYPE_CONSTANT = "TYPE_CONSTANT";

    String TYPE_INPUT = "TYPE_INPUT";

    String TYPE_READ_ONLY_SCRIPT = "TYPE_READ_ONLY_SCRIPT";

    String TYPE_VARIABLE = "TYPE_VARIABLE";

    String TYPE_TRANSIENT_VARIABLE = "TYPE_TRANSIENT_VARIABLE";

    String TYPE_PATTERN = "TYPE_PATTERN";

    String TYPE_JAVA_METHOD_CALL = "TYPE_JAVA_METHOD_CALL";

    String TYPE_PARAMETER = "TYPE_PARAMETER";

    String TYPE_DOCUMENT = "TYPE_DOCUMENT";

    String TYPE_DOCUMENT_LIST = "TYPE_DOCUMENT_LIST";

    String TYPE_ENGINE_CONSTANT = "TYPE_ENGINE_CONSTANT";

    String TYPE_LIST = "TYPE_LIST";

    String TYPE_XPATH_READ = "TYPE_XPATH_READ";

    String TYPE_BUSINESS_DATA = "TYPE_BUSINESS_DATA";

    String TYPE_BUSINESS_DATA_REFERENCE = "TYPE_BUSINESS_DATA_REFERENCE";

    String TYPE_BUSINESS_OBJECT_DAO = "TYPE_BUSINESS_OBJECT_DAO";

    String TYPE_QUERY_BUSINESS_DATA = "TYPE_QUERY_BUSINESS_DATA";

    String TYPE_CONTRACT_INPUT = "TYPE_CONTRACT_INPUT";

    ExpressionKind KIND_CONSTANT = new ExpressionKind(TYPE_CONSTANT);

    ExpressionKind KIND_READ_ONLY_SCRIPT_GROOVY = new ExpressionKind(TYPE_READ_ONLY_SCRIPT, INTERPRETER_GROOVY);

    ExpressionKind KIND_INPUT = new ExpressionKind(TYPE_INPUT);

    ExpressionKind KIND_VARIABLE = new ExpressionKind(TYPE_VARIABLE);

    ExpressionKind KIND_TRANSIENT_VARIABLE = new ExpressionKind(TYPE_TRANSIENT_VARIABLE);

    ExpressionKind KIND_PATTERN = new ExpressionKind(TYPE_PATTERN);

    ExpressionKind KIND_JAVA_METHOD_CALL = new ExpressionKind(TYPE_JAVA_METHOD_CALL);

    ExpressionKind KIND_PARAMETER = new ExpressionKind(TYPE_PARAMETER);

    ExpressionKind KIND_DOCUMENT = new ExpressionKind(TYPE_DOCUMENT);

    ExpressionKind KIND_DOCUMENT_LIST = new ExpressionKind(TYPE_DOCUMENT_LIST);

    ExpressionKind KIND_ENGINE_CONSTANT = new ExpressionKind(TYPE_ENGINE_CONSTANT);

    ExpressionKind KIND_LIST = new ExpressionKind(TYPE_LIST);

    ExpressionKind KIND_XPATH_READ = new ExpressionKind(TYPE_XPATH_READ);

    ExpressionKind KIND_BUSINESS_DATA = new ExpressionKind(TYPE_BUSINESS_DATA);

    ExpressionKind KIND_BUSINESS_DATA_REFERENCE = new ExpressionKind(TYPE_BUSINESS_DATA_REFERENCE);

    ExpressionKind KIND_BUSINESS_OBJECT_DAO = new ExpressionKind(TYPE_BUSINESS_OBJECT_DAO);

    ExpressionKind KIND_QUERY_BUSINESS_DATA = new ExpressionKind(TYPE_QUERY_BUSINESS_DATA);

    ExpressionKind KIND_CONTRACT_INPUT = new ExpressionKind(TYPE_CONTRACT_INPUT);

    /**
     * This list must contain only types with no dependencies
     */
    List<ExpressionKind> NO_DEPENDENCY_EXPRESSION_EVALUATION_ORDER = Arrays.asList(KIND_ENGINE_CONSTANT, KIND_VARIABLE, KIND_CONSTANT, KIND_INPUT,
            KIND_PARAMETER, KIND_DOCUMENT, KIND_BUSINESS_DATA, KIND_BUSINESS_OBJECT_DAO, KIND_CONTRACT_INPUT /*
             * , KIND_PATTERN, KIND_READ_ONLY_SCRIPT_GROOVY,
             * KIND_LIST
             */);

    /**
     * @param expression
     *            the expression to evaluate
     * @param context
     *            map containing the result of the evaluation of dependencies
     *            and also informations about the context of evaluation given by {@link #CONTAINER_ID_KEY} and {@link #CONTAINER_TYPE_KEY}
     * @return
     *         the result of the evaluation of the expression of appropriate type
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     */
    Object evaluate(SExpression expression, Map<String, Object> context, Map<Integer, Object> resolvedExpressions, ContainerState containerState)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException;

    /**
     * Validate the expression, an exception is thrown it is invalid
     *
     * @param expression
     *            the expression to validate
     * @throws SInvalidExpressionException
     *             if the exception is invalid
     * @since 6.0
     */
    void validate(SExpression expression) throws SInvalidExpressionException;

    ExpressionKind getExpressionKind();

    List<Object> evaluate(List<SExpression> expressions, Map<String, Object> context, Map<Integer, Object> resolvedExpressions, ContainerState containerState)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException;

    /**
     * Should we put the evaluated expressions of this strategy in the evaluation context?
     */
    boolean mustPutEvaluatedExpressionInContext();

}
