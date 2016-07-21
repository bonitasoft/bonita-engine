/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.expression.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionType;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class ExpressionImplTest {

    private ExpressionImpl expression;

    @Before
    public void setup() {
        // given:
        expression = new ExpressionImpl();
        expression.setName("toto");
        expression.setContent("value");
        expression.setExpressionType(ExpressionType.TYPE_CONSTANT.name());
        expression.setReturnType(String.class.getName());

        final ExpressionImpl dep = new ExpressionImpl();
        dep.setName("mydep");
        dep.setContent("une dépendance à évaluer");
        expression.setDependencies(Collections.<Expression> singletonList(dep));
    }

    @Test
    public void copy_should_generate_different_ids() throws Exception {
        // when:
        final Expression copy = expression.copy();

        // then:
        assertThat(copy.getId()).isNotZero();
        assertThat(copy.getId()).isNotEqualTo(expression.getId());
        assertThat(copy.getDependencies().get(0).getId()).isNotEqualTo(expression.getDependencies().get(0).getId());
    }

    @Test
    public void copy_should_copy_field_values() throws Exception {
        // when:
        final Expression copy = expression.copy();

        // then:
        assertThat(copy.getContent()).isEqualTo(expression.getContent());
        assertThat(copy.getExpressionType()).isEqualTo(expression.getExpressionType());
        assertThat(copy.getName()).isEqualTo(expression.getName());
        assertThat(copy.getInterpreter()).isEqualTo(expression.getInterpreter());
        assertThat(copy.getReturnType()).isEqualTo(expression.getReturnType());
        assertThat(copy.getDependencies().get(0).getContent()).isEqualTo(expression.getDependencies().get(0).getContent());
    }

    @Test
    public void constructor_should_not_set_id() throws Exception {
        // then:
        assertThat(expression.getId()).isZero();
    }

}
