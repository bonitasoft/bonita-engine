/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.expression;

import static org.junit.jupiter.api.Assertions.*;

import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.impl.SExpressionBuilderFactoryImpl;
import org.bonitasoft.engine.parameter.ParameterService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterExpressionExecutorStrategyTest {

    @Mock
    private ParameterService parameterService;

    @ParameterizedTest
    @CsvSource(value = { "hello", "some_password", "Attention", "$hello1" })
    void validParameterExpressionContent(String content) throws SInvalidExpressionException {
        var strategy = new ParameterExpressionExecutorStrategy(parameterService);

        assertDoesNotThrow(() -> strategy.validate(parameterExpression(content)));
    }

    @ParameterizedTest
    @CsvSource(value = { "1hello", ")hello", "(hello", "hel\\lo", "hel/lo", "switch", "for" })
    void invalidParameterExpressionContent(String content) {
        var strategy = new ParameterExpressionExecutorStrategy(parameterService);

        assertThrows(SInvalidExpressionException.class, () -> strategy.validate(parameterExpression(content)));
    }

    private static SExpression parameterExpression(String content) throws SInvalidExpressionException {
        return new SExpressionBuilderFactoryImpl().createNewInstance()
                .setExpressionType(ExpressionType.TYPE_PARAMETER.name())
                .setName(content)
                .setContent(content)
                .setReturnType(String.class.getName())
                .done();
    }
}
