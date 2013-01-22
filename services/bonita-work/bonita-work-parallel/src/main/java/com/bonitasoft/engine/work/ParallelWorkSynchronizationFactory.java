/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.work;

import java.util.concurrent.ExecutorService;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.AbstractWorkSynchronization;
import org.bonitasoft.engine.work.ExecutorWorkService;
import org.bonitasoft.engine.work.WorkSynchronizationFactory;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class ParallelWorkSynchronizationFactory implements WorkSynchronizationFactory {

    @Override
    public AbstractWorkSynchronization getWorkSynchronization(final ExecutorService executorService, final TechnicalLoggerService loggerService,
            final SessionAccessor sessionAccessor, final SessionService sessionService, final TransactionService transactionService,
            final ExecutorWorkService threadPoolWorkService) {
        return new ParallelWorkSynchronization(executorService, loggerService, sessionAccessor, sessionService, transactionService, threadPoolWorkService);
    }

}
