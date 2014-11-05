package org.bonitasoft.engine.core.process.definition.model.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;

public class ServerModelConvertorTest {

    Expression buildClientExpression(final String name, final String content, final String expressionType, final String returnType,
            final List<SExpression> dependencies) {
        final ExpressionImpl expression = new ExpressionImpl();
        expression.setName(name);
        expression.setContent(content);
        expression.setExpressionType(expressionType);
        expression.setReturnType(returnType);
        return expression;
    }

    @Test
    public void convert_a_business_query_to_a_server_one() {
        final String name = "expressionName";
        final String content = "getEmployees";
        final String expressionType = ExpressionType.TYPE_QUERY_BUSINESS_DATA.name();
        final String returnType = "org.bonitasoft.Employee";
        final Expression clientExpression = buildClientExpression(name, content, expressionType, returnType, null);
        final SExpressionImpl expected = new SExpressionImpl(name, content, expressionType, returnType, null, Collections.<SExpression> emptyList());

        final SExpression expression = ServerModelConvertor.convertExpression(clientExpression);

        assertThat(expression).isEqualTo(expected);
    }

    @Test
    public void return_null_when_converting_a_null_business_data() {
        final SBusinessDataDefinition businessDataDefinition = ServerModelConvertor.convertBusinessDataDefinition(null);
        assertThat(businessDataDefinition).isNull();
    }

}
