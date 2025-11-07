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
package org.bonitasoft.engine.classloader;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for triggering the refresh of classloaders in separated threads/transactions
 * It is useful when refreshing classloader at the end of the transaction when one transaction is still active and
 * we need to open a new one.
 */
@Slf4j
@Component
class ClassLoaderUpdater {

    final static long DEFAULT_CLASSLOADER_INITIALIZATION_TIMEOUT_MINUTES = 5L;

    private final BonitaTaskExecutor bonitaTaskExecutor;
    private final SessionAccessor sessionAccessor;
    private final UserTransactionService userTransactionService;

    @Value("${bonita.runtime.classloader.initialization.timeout-minutes:-1}")
    private long classLoaderInitializationTimeoutMinutes;

    public ClassLoaderUpdater(BonitaTaskExecutor bonitaTaskExecutor,
            SessionAccessor sessionAccessor, UserTransactionService userTransactionService) {
        this.bonitaTaskExecutor = bonitaTaskExecutor;
        this.sessionAccessor = sessionAccessor;
        this.userTransactionService = userTransactionService;
    }

    public void refreshClassloaders(ClassLoaderServiceImpl classLoaderService, Long tenantId,
            Set<ClassLoaderIdentifier> ids) {

        execute(tenantId, () -> {
            for (ClassLoaderIdentifier id : ids) {
                classLoaderService.refreshClassLoaderImmediately(id);
            }
            return null;
        });
    }

    BonitaClassLoader initializeClassLoader(ClassLoaderServiceImpl classLoaderService,
            ClassLoaderIdentifier identifier) {
        log.debug("Request creation of classloader in an other thread: {}. A {} minutes timeout will be used.",
                identifier, classLoaderInitializationTimeoutMinutes);
        return execute(getTenantId(), () -> classLoaderService.createClassloader(identifier));
    }

    private <T> T execute(Long tenantId, Callable<T> callable) {
        Future<T> execute = bonitaTaskExecutor.execute(
                inSession(tenantId, inTransaction(callable)));
        try {
            return execute.get(classLoaderInitializationTimeoutMinutes, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException e) {
            throw new SBonitaRuntimeException("Unable to refresh the classloaders", e);
        } catch (TimeoutException toe) {
            throw new SBonitaRuntimeException("Unable to refresh the classloaders within "
                    + classLoaderInitializationTimeoutMinutes
                    + " minutes. You may want to adjust the timeout using either the "
                    + "bonita.runtime.classloader.initialization.timeout-minutes property or the "
                    + "BONITA_RUNTIME_CLASSLOADER_INITIALIZATION_TIMEOUT_MINUTES environment variable."
                    + "Make sure the value is within [1..XA_TRANSACTION_TIMEOUT].",
                    toe);
        }
    }

    private <T> Callable<T> inSession(Long tenantId, Callable<T> callable) {
        if (tenantId == null) {
            return callable;
        }
        return () -> {
            sessionAccessor.setTenantId(tenantId);
            try {
                return callable.call();
            } finally {
                sessionAccessor.deleteTenantId();
            }
        };
    }

    private <T> Callable<T> inTransaction(Callable<T> callable) {
        return () -> userTransactionService.executeInTransaction(callable);
    }

    private Long getTenantId() {
        try {
            return sessionAccessor.getTenantId();
        } catch (STenantIdNotSetException ignored) {
            //In a platform session
        }
        return null;
    }

    @PostConstruct
    void validateClassLoaderInitializationTimeoutMinutes() {
        if (classLoaderInitializationTimeoutMinutes <= 0) {
            classLoaderInitializationTimeoutMinutes = DEFAULT_CLASSLOADER_INITIALIZATION_TIMEOUT_MINUTES;
        }
        log.debug(
                "Using a timeout of {} minutes for class loader initialization or refresh. This can be configured "
                        + "via the bonita.runtime.classloader.initialization.timeout-minutes property or the "
                        + "BONITA_RUNTIME_CLASSLOADER_INITIALIZATION_TIMEOUT_MINUTES environment variable. "
                        + "Make sure the value is within [1..XA_TRANSACTION_TIMEOUT].",
                classLoaderInitializationTimeoutMinutes);
    }

}
