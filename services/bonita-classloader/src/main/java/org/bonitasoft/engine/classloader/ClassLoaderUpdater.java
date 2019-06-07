package org.bonitasoft.engine.classloader;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;

public class ClassLoaderUpdater {


    private BonitaTaskExecutor bonitaTaskExecutor;
    private SessionAccessor sessionAccessor;
    private UserTransactionService userTransactionService;

    public ClassLoaderUpdater(BonitaTaskExecutor bonitaTaskExecutor,
                              SessionAccessor sessionAccessor, UserTransactionService userTransactionService) {
        this.bonitaTaskExecutor = bonitaTaskExecutor;
        this.sessionAccessor = sessionAccessor;
        this.userTransactionService = userTransactionService;
    }

    public void refreshClassloaders(ClassLoaderService classLoaderService, Long tenantId, Set<Pair<ScopeType, Long>> ids) {

        Future<Void> execute = bonitaTaskExecutor.execute(
                inSession(tenantId, inTransaction(() -> {
                    for (Pair<ScopeType, Long> id : ids) {
                        classLoaderService.refreshClassLoader(id.getKey(), id.getValue());
                    }
                    return null;
                })));
        try {
            execute.get(5, TimeUnit.MINUTES);//hard coded timeout, it should never happen
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SBonitaRuntimeException("Unable to refresh the classloaders: "+ids, e);
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

}
