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
package org.bonitasoft.engine.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.NOT_RETRYABLE;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.RETRYABLE;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.UNCERTAIN_COMPLETION_OF_COMMIT;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.xa.XAException;

import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.junit.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * @author Baptiste Mesta.
 */
public class DefaultExceptionRetryabilityEvaluatorTest {

    private DefaultExceptionRetryabilityEvaluator defaultExceptionRetryabilityEvaluator;

    @Test
    public void should_be_retryable_if_exception_is_retryable() {
        initWith(SRetryableException.class);

        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new SRetryableException(new IllegalStateException("a retryable exception")));

        assertThat(shouldBeRetried).isEqualTo(RETRYABLE);
    }

    @Test
    public void should_be_retryable_if_some_exception_in_hierarchy_is_retryable() {
        initWith(SRetryableException.class);

        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new IllegalStateException(
                        (new SRetryableException(new IllegalStateException("a retryable exception")))));

        assertThat(shouldBeRetried).isEqualTo(RETRYABLE);
    }

    private static class MySRetryableException extends SRetryableException {

        public MySRetryableException() {
            super(new IllegalArgumentException());
        }
    }

    @Test
    public void should_be_retryable_if_some_exception_is_extending_a_retryable() {
        initWith(SRetryableException.class);

        SRetryableException myRetryable = new MySRetryableException();
        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new IllegalStateException(
                        (myRetryable)));

        assertThat(shouldBeRetried).isEqualTo(RETRYABLE);
    }

    @Test
    public void should_be_retryable_if_exception_is_retryable_with_multiple_exception_in_configuration() {
        initWith(SRetryableException.class, PortUnreachableException.class);

        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new IllegalStateException(
                        (new PortUnreachableException("unreached port"))));

        assertThat(shouldBeRetried).isEqualTo(RETRYABLE);
    }

    @Test
    public void should_be_not_retryable_if_exception_is_not_a_retryable() {
        initWith(SRetryableException.class);

        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new IllegalStateException("a retryable exception"));

        assertThat(shouldBeRetried).isEqualTo(NOT_RETRYABLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_constructor_fail_if_classname_do_not_exists() {
        new DefaultExceptionRetryabilityEvaluator(Collections.singletonList("unkown class"), Collections.emptyList(),
                new TechnicalLoggerSLF4JImpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_constructor_fail_if_class_is_not_throwable() {
        new DefaultExceptionRetryabilityEvaluator(Collections.singletonList("java.util.List"), Collections.emptyList(),
                new TechnicalLoggerSLF4JImpl());
    }

    @Test
    public void should_be_not_retryable_if_contains_retryable_exception_with_caused_by_blacklisted_exception() {
        initWithBlackListed(SRetryableException.class, IllegalStateException.class);

        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new SRetryableException(new IllegalStateException()));

        assertThat(shouldBeRetried).isEqualTo(NOT_RETRYABLE);
    }

    @Test
    public void should_be_not_retryable_if_contains_blacklisted_exception_even_if_cause_is_retryable() {
        initWithBlackListed(SRetryableException.class, IllegalStateException.class);

        ExceptionRetryabilityEvaluator.Retryability shouldBeRetried = defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new IllegalStateException(new SRetryableException(new IOException())));

        assertThat(shouldBeRetried).isEqualTo(NOT_RETRYABLE);
    }

    @Test
    public void should_retry_sql_connection_exceptions() {
        initWith(SRetryableException.class);

        assertThat(defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new IllegalStateException(new SQLException("connection issue"))))
                        .isEqualTo(RETRYABLE);
        assertThat(defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new CannotGetJdbcConnectionException("I/O error"))).isEqualTo(RETRYABLE);
    }

    @Test
    public void should_be_UNCERTAIN_COMPLETION_OF_COMMIT_when_retryable_exception_happen_on_commit() {
        initWith(SRetryableException.class);

        assertThat(defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new STransactionCommitException(new SQLException("connection issue"))))
                        .isEqualTo(UNCERTAIN_COMPLETION_OF_COMMIT);
    }

    @Test
    public void should_not_be_UNCERTAIN_COMPLETION_OF_COMMIT_when_retryable_exception_happen_on_commit_but_wat_not_connection_issue() {
        initWith(SRetryableException.class);

        assertThat(defaultExceptionRetryabilityEvaluator.evaluateRetryability(
                new STransactionCommitException(new SRetryableException(new IOException("some file issue")))))
                        .isEqualTo(RETRYABLE);
    }

    @Test
    public void should_retry_retryable_xa_exceptions() {
        initWithBlackListed(SRetryableException.class, IllegalStateException.class);

        assertThat(
                defaultExceptionRetryabilityEvaluator.evaluateRetryability(new XAException(XAException.XA_RBTIMEOUT)))
                        .isEqualTo(RETRYABLE);
    }

    @Test
    public void should_retry_when_exception_is_nested() {
        initWithBlackListed(SQLTransientException.class, IllegalStateException.class);

        assertThat(defaultExceptionRetryabilityEvaluator
                .evaluateRetryability(new ExceptionThatHasNested(new SQLTransientException()))).isEqualTo(RETRYABLE);
    }

    @SafeVarargs
    private final void initWith(Class<? extends Throwable>... throwables) {
        defaultExceptionRetryabilityEvaluator = new DefaultExceptionRetryabilityEvaluator(toListNames(throwables),
                Collections.emptyList(), new TechnicalLoggerSLF4JImpl());
    }

    @SafeVarargs
    private final void initWithBlackListed(Class<? extends Throwable> toRetry,
            Class<? extends Throwable>... blackListed) {
        defaultExceptionRetryabilityEvaluator = new DefaultExceptionRetryabilityEvaluator(
                toListNames(toRetry), toListNames(blackListed), new TechnicalLoggerSLF4JImpl());
    }

    private List<String> toListNames(Class<? extends Throwable>... blackListed) {
        return Arrays.asList(blackListed).stream().map(Class::getName).collect(Collectors.toList());
    }

    class ExceptionThatHasNested extends Exception {

        List<Throwable> exceptions;

        ExceptionThatHasNested(Throwable... exceptions) {
            super("exception with nested");
            this.exceptions = Arrays.asList(exceptions);
        }

        public List<Throwable> getExceptions() {
            return exceptions;
        }
    }
}
