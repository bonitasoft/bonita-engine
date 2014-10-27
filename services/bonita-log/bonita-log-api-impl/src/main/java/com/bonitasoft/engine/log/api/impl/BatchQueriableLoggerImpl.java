/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;
import org.bonitasoft.engine.services.impl.AbstractQueriableLoggerImpl;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BatchQueriableLoggerImpl extends AbstractQueriableLoggerImpl {

    private final TransactionService transactionService;

    private final PersistenceService persistenceService;

    private final boolean delayable;

    private final ThreadLocal<BatchLogSynchronization> synchronizations = new ThreadLocal<BatchLogSynchronization>();

    public BatchQueriableLoggerImpl(final PersistenceService persistenceService, final TransactionService transactionService,
            final QueriableLoggerStrategy loggerStrategy, final QueriableLogSessionProvider sessionProvider,
            final TechnicalLoggerService logger, final PlatformService platformService, final Boolean delayable) {
        super(persistenceService, loggerStrategy, sessionProvider, platformService, logger);
        this.persistenceService = persistenceService;
        this.transactionService = transactionService;
        this.delayable = delayable;
    }

    protected synchronized BatchLogSynchronization getBatchLogSynchronization() throws STransactionNotFoundException {
        BatchLogSynchronization synchro = synchronizations.get();
        if (synchro == null) {
            synchro = new BatchLogSynchronization(persistenceService, BatchLogBuffer.getInstance(), delayable, this);
            synchronizations.set(synchro);
            transactionService.registerBonitaSynchronization(synchro);
        }
        return synchro;
    }

    @Override
    protected void log(final List<SQueriableLog> loggableLogs) {
        BatchLogSynchronization synchro;
        try {
            synchro = getBatchLogSynchronization();
            for (final SQueriableLog sQueriableLog : loggableLogs) {
                synchro.addLog(sQueriableLog);
            }
        } catch (final STransactionNotFoundException e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register synchronization to log queriable logs: transaction not found");
        }
    }

    protected void cleanSynchronization() {
        synchronizations.remove();
    }

}
