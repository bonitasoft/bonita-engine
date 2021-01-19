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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaResource;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.stereotype.Component;

@Component
class ClassLoaderUpdater {

    private BonitaTaskExecutor bonitaTaskExecutor;
    private SessionAccessor sessionAccessor;
    private UserTransactionService userTransactionService;

    public ClassLoaderUpdater(BonitaTaskExecutor bonitaTaskExecutor,
            SessionAccessor sessionAccessor, UserTransactionService userTransactionService) {
        this.bonitaTaskExecutor = bonitaTaskExecutor;
        this.sessionAccessor = sessionAccessor;
        this.userTransactionService = userTransactionService;
    }

    public void refreshClassloaders(ClassLoaderServiceImpl classLoaderService, Long tenantId,
            Set<Pair<ScopeType, Long>> ids) {

        execute(tenantId, () -> {
            for (Pair<ScopeType, Long> id : ids) {
                classLoaderService.refreshClassLoaderImmediately(id.getKey(), id.getValue());
            }
            return null;
        });
    }

    void initializeClassLoader(ClassLoaderServiceImpl classLoaderService, VirtualClassLoader virtualClassLoader,
            ClassLoaderIdentifier identifier) {
        execute(getTenantId(), () -> {
            doInitializeClassLoader(classLoaderService, virtualClassLoader, identifier);
            return null;
        });
    }

    private void doInitializeClassLoader(ClassLoaderServiceImpl classLoaderService,
            VirtualClassLoader virtualClassLoader, ClassLoaderIdentifier identifier)
            throws SClassLoaderException, BonitaHomeNotSetException, IOException {
        Stream<BonitaResource> dependencies = classLoaderService.getDependencies(identifier.getScopeType(),
                identifier.getId());
        BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(dependencies, identifier.getType(),
                identifier.getId(),
                classLoaderService.getLocalTemporaryFolder(identifier.getType(), identifier.getId()),
                virtualClassLoader.getParent());
        virtualClassLoader.replaceClassLoader(bonitaClassLoader);
    }

    private void execute(Long tenantId, Callable<Void> callable) {
        Future<Void> execute = bonitaTaskExecutor.execute(
                inSession(tenantId, inTransaction(callable)));
        try {
            execute.get(5, TimeUnit.MINUTES);//hard coded timeout, it should never happen
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SBonitaRuntimeException("Unable to refresh the classloaders", e);
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
}
