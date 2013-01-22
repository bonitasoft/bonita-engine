/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.work;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.AbstractWorkSynchronization;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.RunnableListener;
import org.bonitasoft.engine.work.ExecutorWorkService;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class ParallelWorkSynchronization extends AbstractWorkSynchronization {

    private final RunnableListener runnableListener;

    public ParallelWorkSynchronization(final ExecutorService executorService, final TechnicalLoggerService loggerService,
            final SessionAccessor sessionAccessor, final SessionService sessionService, final TransactionService transactionService,
            final ExecutorWorkService threadPoolWorkService) {
        super(threadPoolWorkService, executorService, loggerService, sessionAccessor, sessionService, transactionService);
        runnableListener = threadPoolWorkService;
    }

    @Override
    protected void executeRunnables(final Collection<BonitaWork> works) {
        for (final BonitaWork work : works) {
            executorService.submit(new BonitaWorkWrapper(runnableListener, getTenantId(), work));
        }
    }
}
