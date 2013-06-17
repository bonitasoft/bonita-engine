/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SRetryableException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class RetryTransactionExecutor implements TransactionExecutor {

    private final TransactionService transactionService;

    private final TechnicalLoggerService loggerService;

    private final int retries;

    private final long delay;

    private final double delayFactor;

    public RetryTransactionExecutor(final TransactionService transactionService, final TechnicalLoggerService loggerService, final int retries,
            final long delay, final double delayFactor) {
        this.transactionService = transactionService;
        this.loggerService = loggerService;
        this.retries = retries;
        this.delay = delay;
        this.delayFactor = delayFactor;
    }

    @Override
    public void execute(final TransactionContent transactionContent) throws SBonitaException {
        execute(Arrays.asList(transactionContent));
    }

    @Override
    public void execute(final TransactionContent... transactionContents) throws SBonitaException {
        execute(Arrays.asList(transactionContents));
    }

    @Override
    public void execute(final List<TransactionContent> transactionContents) throws SBonitaException {
        int attempt = 0;
        long sleepTime = delay;
        while (attempt <= retries) {
            final boolean txOpened = openTransaction();
            try {
                for (final TransactionContent transactionContent : transactionContents) {
                    transactionContent.execute();
                }
                attempt = retries + 1;
            } catch (final SRetryableException sre) {
                setTransactionRollback();
                attempt++;
                if (loggerService.isLoggable(TransactionExecutor.class, TechnicalLogSeverity.INFO)) {
                    loggerService.log(TransactionExecutor.class, TechnicalLogSeverity.INFO, "Transaction failed", sre);
                    loggerService.log(TransactionExecutor.class, TechnicalLogSeverity.INFO, "Retring(# " + attempt + ") in " + sleepTime + " ms");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (final InterruptedException ie) {
                    if (loggerService.isLoggable(TransactionExecutor.class, TechnicalLogSeverity.TRACE)) {
                        loggerService.log(TransactionExecutor.class, TechnicalLogSeverity.TRACE, "Retry Sleeping was interrupted!");
                    }
                }
                sleepTime *= delayFactor;
            } catch (final SBonitaException sbe) {
                setTransactionRollback();
                throw sbe;
            } finally {
                completeTransaction(txOpened);
            }
        }
    }

    @Override
    public boolean openTransaction() throws STransactionException {
        if (transactionService.isTransactionActive()) {
            return false;
        } else {
            try {
                transactionService.begin();
                return true;
            } catch (final STransactionCreationException e) {
                throw new STransactionException("Unable to open transaction, transaction state is " + transactionService.getState(), e);
            }
        }
    }

    @Override
    public void completeTransaction(final boolean txOpened) throws STransactionException {
        if (txOpened) {
            transactionService.complete();
        }
    }

    @Override
    public void setTransactionRollback() throws STransactionException {
        transactionService.setRollbackOnly();
    }

}
