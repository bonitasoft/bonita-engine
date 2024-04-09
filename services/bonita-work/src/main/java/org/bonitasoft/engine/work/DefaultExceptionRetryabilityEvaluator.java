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

import static javax.transaction.xa.XAException.XA_RBTIMEOUT;
import static javax.transaction.xa.XAException.XA_RBTRANSIENT;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.NOT_RETRYABLE;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.RETRYABLE;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.UNCERTAIN_COMPLETION_OF_COMMIT;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.transaction.xa.XAException;

import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

/**
 * @author Baptiste Mesta.
 */

public class DefaultExceptionRetryabilityEvaluator implements ExceptionRetryabilityEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionRetryabilityEvaluator.class);
    private static final List<Integer> XA_RETRYABLE = Arrays.asList(XA_RBTRANSIENT, XA_RBTIMEOUT);
    private HashSet<Class<? extends Throwable>> exceptionClassesToRetry;
    private HashSet<Class<? extends Throwable>> exceptionClassesToNotRetry;

    public DefaultExceptionRetryabilityEvaluator(List<String> exceptionClassesToRetry,
            List<String> exceptionClassesToNotRetry) {
        this.exceptionClassesToRetry = toSetOfClasses(exceptionClassesToRetry);
        this.exceptionClassesToNotRetry = toSetOfClasses(exceptionClassesToNotRetry);
    }

    private HashSet<Class<? extends Throwable>> toSetOfClasses(List<String> exceptionClassesToRetry) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        HashSet<Class<? extends Throwable>> classSet = new HashSet<>();
        for (String className : exceptionClassesToRetry) {
            classSet.add(getCheckedThrowable(classLoader, className));
        }
        return classSet;
    }

    private Class<? extends Throwable> getCheckedThrowable(ClassLoader classLoader, String className) {
        Class<?> throwable;
        try {
            throwable = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("The class %s is not found", className), e);
        }
        if (!Throwable.class.isAssignableFrom(throwable)) {
            throw new IllegalArgumentException(String.format("The class %s is not a Throwable", className));
        }
        return (Class<? extends Throwable>) throwable;
    }

    @Override
    public Retryability evaluateRetryability(Throwable thrown) {
        List<Throwable> exceptions = getAllCauses(thrown);
        Optional<Throwable> notRetryableException = exceptions.stream().filter(this::isKnownAsNotRetryable).findFirst();
        if (notRetryableException.isPresent()) {
            logger.debug("Exception is known as not retryable: {}", print(exceptions, notRetryableException.get()));
            return NOT_RETRYABLE;
        }
        Optional<Throwable> retryableException = exceptions.stream().filter(this::isRetryable).findFirst();
        if (!retryableException.isPresent()) {
            logger.debug("No retryable exception found: {}", print(exceptions));
            return NOT_RETRYABLE;
        }

        Throwable theRetryableException = retryableException.get();
        if (isSqlConnectionIssue(theRetryableException)
                && exceptions.stream().anyMatch(e -> e instanceof STransactionCommitException)) {
            logger.debug(
                    "Retryable exception found but exception happened on commit, uncertain completion of the transaction: {}",
                    print(exceptions, theRetryableException));
            return UNCERTAIN_COMPLETION_OF_COMMIT;
        } else {
            logger.debug("Retryable exception found: {}", print(exceptions, theRetryableException));
            return RETRYABLE;
        }
    }

    private boolean isRetryable(Throwable thrown) {
        if (isInListedRetriableException(thrown)) {
            return true;
        }
        if (thrown instanceof SQLException) {
            DataAccessException dataAccessException = new SQLErrorCodeSQLExceptionTranslator().translate("", "",
                    (SQLException) thrown);
            if (dataAccessException instanceof RecoverableDataAccessException
                    || dataAccessException instanceof TransientDataAccessException) {
                return true;
            }
        }
        if (thrown instanceof XAException) {
            int errorCode = ((XAException) thrown).errorCode;
            if (XA_RETRYABLE.contains(errorCode)) {
                return true;
            }
        }
        return isSqlConnectionIssue(thrown);
    }

    private List<Throwable> getAllCauses(Throwable thrown) {
        List<Throwable> allExceptions = new ArrayList<>();
        while (thrown != null) {
            allExceptions.add(thrown);
            List<Exception> exceptions = getExceptions(thrown, "getExceptions", "getHandlerExceptions");
            if (!exceptions.isEmpty()) {
                for (Exception exception : exceptions) {
                    allExceptions.addAll(getAllCauses(exception));
                }
            }
            thrown = thrown.getCause();
        }
        return allExceptions;
    }

    private boolean isSqlConnectionIssue(Throwable thrown) {
        String name = thrown.getClass().getSimpleName().toLowerCase();
        String message = thrown.getMessage() != null ? thrown.getMessage().toLowerCase() : "";
        return (name.contains("sql") || name.contains("jdbc"))
                && (message.contains("connection") || message.contains("timeout") || message.contains("i/o error"));
    }

    private String print(List<Throwable> throwables, Throwable highlighted) {
        StringBuilder stringBuilder = new StringBuilder("Exceptions:\n");
        for (Throwable throwable : throwables) {
            stringBuilder.append(" ↳");
            stringBuilder.append(throwable.getClass().getName());
            if (highlighted != null && throwable.getClass().equals(highlighted.getClass())) {
                stringBuilder.append(": ");
                stringBuilder.append(throwable.getMessage());
                stringBuilder.append(" ☚");
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    private String print(List<Throwable> throwables) {
        return print(throwables, null);
    }

    private boolean isInListedRetriableException(Throwable e) {
        return isListedIn(e, exceptionClassesToRetry);
    }

    private boolean isKnownAsNotRetryable(Throwable e) {
        return isListedIn(e, exceptionClassesToNotRetry);
    }

    private boolean isListedIn(Throwable e, HashSet<Class<? extends Throwable>> exceptions) {
        for (Class<? extends Throwable> throwable : exceptions) {
            if (throwable.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    private List<Exception> getExceptions(Throwable thrown, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                //for bitronix PhaseException and FireEventExceptions
                Method getExceptions = thrown.getClass().getDeclaredMethod(methodName);
                return (List<Exception>) getExceptions.invoke(thrown);
            } catch (Throwable ignored) {
            }
        }
        return Collections.emptyList();
    }
}
