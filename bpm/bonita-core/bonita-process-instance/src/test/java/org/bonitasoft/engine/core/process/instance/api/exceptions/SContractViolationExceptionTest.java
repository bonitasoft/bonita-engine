/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * author Emmanuel Duchastenier
 */
public class SContractViolationExceptionTest {

    @Test
    public void should_return_explanation() {
        final SContractViolationException contractViolationException = new SContractViolationException("Bad contract",
                Arrays.asList("issue1", "issue2"));

        assertThat(contractViolationException.getExplanations()).containsExactly("issue1", "issue2");
    }

    @Test
    public void should_print_stack_trace_show_explanations() {
        final SContractViolationException contractViolationException = new SContractViolationException("Bad contract",
                Arrays.asList("issue1", "issue2"));

        assertThat(contractViolationException.getMessage()).isEqualTo("Bad contract: issue1, issue2");

    }

    @Test
    public void should_print_stack_trace_show_one_explanation() {
        final SContractViolationException contractViolationException = new SContractViolationException("Bad contract",
                Arrays.asList("issue1"));

        assertThat(contractViolationException.getMessage()).isEqualTo("Bad contract: issue1");

    }

    @Test
    public void should_print_stack_trace_with_null_explanations() {
        List<String> explanations = null;
        @SuppressWarnings("ConstantConditions")
        final SContractViolationException contractViolationException = new SContractViolationException("Bad contract",
                explanations);

        assertThat(contractViolationException.getMessage()).isEqualTo("Bad contract: no details");
    }

    @Test
    public void should_print_stack_trace_with_empty_explanations() {
        List<String> explanations = Collections.emptyList();
        final SContractViolationException contractViolationException = new SContractViolationException("Bad contract",
                explanations);

        assertThat(contractViolationException.getMessage()).isEqualTo("Bad contract: no details");
    }

    @Test
    public void should_getSimpleMessage_not_show_explanations() {
        final SContractViolationException contractViolationException = new SContractViolationException("Bad contract",
                Arrays.asList("issue1", "issue2"));

        assertThat(contractViolationException.getSimpleMessage()).isEqualTo("Bad contract");

    }
}
