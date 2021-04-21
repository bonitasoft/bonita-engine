/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.junit.Test;

public class ExceptionUtilsTest {

    @Test
    public void should_print_root_cause_of_an_exception() {
        Exception exception = new Exception("direct exception",
                new Exception("intermediate exception", new SBonitaRuntimeException("This is the root cause")));

        String rootCause = ExceptionUtils.printRootCauseOnly(exception);

        assertThat(rootCause)
                .isEqualTo("org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException: This is the root cause");
    }

    @Test
    public void should_print_root_cause_of_an_exception_when_there_is_no_root_cause() {
        Exception exception = new Exception("direct exception with no root cause");

        String rootCause = ExceptionUtils.printRootCauseOnly(exception);

        assertThat(rootCause).isEqualTo("java.lang.Exception: direct exception with no root cause");
    }

    @Test
    public void should_print_lightweight_stacktrace() {
        Exception exception = doSomeBusiness();

        String lightWeightStacktrace = ExceptionUtils.printLightWeightStacktrace(exception, 3);

        assertThat(lightWeightStacktrace).startsWith(
                "org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException: This is the root cause\n" +
                        "\twrapped by java.lang.Exception: intermediate exception\n" +
                        "\twrapped by java.lang.Exception: Wrap all other exceptions\n" +
                        " exception was generated here:\tat org.bonitasoft.engine.commons.ExceptionUtilsTest.methodThatCauseRootException(ExceptionUtilsTest.java:");

    }

    protected Exception doSomeBusiness() {
        try {
            doSomeBusiness2();
        } catch (Exception e) {
            return new Exception("Wrap all other exceptions", e);
        }
        return null;
    }

    protected void doSomeBusiness2() throws Exception {
        try {
            methodThatCauseRootException();
        } catch (SBonitaRuntimeException e) {
            throw new Exception("intermediate exception", e);
        }
    }

    protected void methodThatCauseRootException() {
        throw new SBonitaRuntimeException("This is the root cause");
    }

}
