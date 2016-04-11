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
package org.bonitasoft.engine.bpm.contract.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class ContractConstraintsValidator {

    private final TechnicalLoggerService logger;
    private final ExpressionService expressionService;

    public ContractConstraintsValidator(final TechnicalLoggerService logger, ExpressionService expressionService) {
        this.logger = logger;
        this.expressionService = expressionService;
    }

    public void validate(long processDefinitionId, final SContractDefinition contract, final Map<String, Serializable> variables)
            throws SContractViolationException {
        final Map<String, Serializable> vars = (variables == null ? Collections.<String, Serializable> emptyMap() : variables);
        final List<String> comments = new ArrayList<>();
        final Map<String, Object> context = new HashMap<>(vars.size());
        context.putAll(vars);
        context.put(ExpressionExecutorStrategy.DEFINITION_ID, processDefinitionId);
        for (final SConstraintDefinition constraint : contract.getConstraints()) {
            if (logger.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG)) {
                logger.log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG, "Evaluating constraint [" + constraint.getName() + "] on input(s) "
                        + constraint.getInputNames());
            }
            validateConstraint(comments, constraint, context);
        }
        if (!comments.isEmpty()) {
            throw new SContractViolationException("Error while validating constraints", comments);
        }
    }

    private void validateConstraint(final List<String> comments, final SConstraintDefinition constraint, final Map<String, Object> variables)
            throws SContractViolationException {
        Boolean valid;
        try {
            valid = (Boolean) expressionService.evaluate(createGroovyExpression(constraint), variables, Collections.<Integer, Object> emptyMap(),
                    ContainerState.ACTIVE);
        } catch (SExpressionTypeUnknownException | SExpressionEvaluationException | SExpressionDependencyMissingException | SInvalidExpressionException e) {
            logger.log(ContractConstraintsValidator.class, TechnicalLogSeverity.ERROR,
                    "Constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames() + " evaluation failed.", e);
            throw new SContractViolationException("Exception while validating constraints", e);
        }
        if (!valid) {
            logger.log(ContractConstraintsValidator.class, TechnicalLogSeverity.WARNING,
                    "Constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames() + " is not valid");
            comments.add(constraint.getExplanation());
        }
    }

    private SExpression createGroovyExpression(SConstraintDefinition constraint) throws SInvalidExpressionException {
        return BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance().setName(constraint.getName()).setContent(constraint.getExpression())
                .setExpressionType(ExpressionExecutorStrategy.TYPE_READ_ONLY_SCRIPT).setInterpreter(ExpressionExecutorStrategy.INTERPRETER_GROOVY)
                .setReturnType(Boolean.class.getName()).done();
    }

}
