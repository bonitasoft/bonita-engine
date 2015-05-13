/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.bpm.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ContractViolationExceptionTest {

    @Test
    public void should_return_explanation(){
        final ContractViolationException contractViolationException = new ContractViolationException("Bad contract", Arrays.asList("issue1", "issue2"));

        assertThat(contractViolationException.getExplanations()).containsExactly("issue1", "issue2");
    }
    @Test
    public void should_print_stack_trace_show_explanations(){
        final ContractViolationException contractViolationException = new ContractViolationException("Bad contract", Arrays.asList("issue1", "issue2"));

        assertThat(contractViolationException.getMessage()).isEqualTo("Bad contract: [issue1, issue2]");

    }
    @Test
    public void should_getSimpleMessage_not_show_explanations() {
        final ContractViolationException contractViolationException = new ContractViolationException("Bad contract", Arrays.asList("issue1", "issue2"));

        assertThat(contractViolationException.getSimpleMessage()).isEqualTo("Bad contract");

    }
}