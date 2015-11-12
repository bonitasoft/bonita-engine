/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.transaction;

import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SRetryableException;
import org.bonitasoft.engine.transaction.JTATransactionServiceImpl;

/**
 * @author Laurent Vaills
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class JTATransactionServiceExt extends JTATransactionServiceImpl {

    private final int retries;

    private final long delay;

    private final double delayFactor;

    public JTATransactionServiceExt(final TechnicalLoggerService logger, final TransactionManager txManager, final int retries, final long delay,
            final double delayFactor) {
        super(logger, txManager);
        this.retries = retries;
        this.delay = delay;
        this.delayFactor = delayFactor;
    }

    @Override
    public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
        int attempt = 0;
        long sleepTime = delay;
        SRetryableException lastException = null;
        do {
            begin();
            try {
                return callable.call();
            } catch (final SRetryableException e) {
                lastException = e;
                setRollbackOnly();
                if (logger.isLoggable(JTATransactionServiceExt.class, TechnicalLogSeverity.INFO)) {
                    logger.log(JTATransactionServiceExt.class, TechnicalLogSeverity.INFO, "Transaction failed", e);
                    logger.log(JTATransactionServiceExt.class, TechnicalLogSeverity.INFO, "Retrying (# " + attempt + "/" + retries + ") in " + sleepTime
                            + " ms");
                }
            } catch (final Exception e) {
                setRollbackOnly();
                throw e;
            } finally {
                complete();
            }
            sleep(sleepTime);
            sleepTime *= delayFactor;
            attempt++;
        } while (attempt <= retries);
        // after all attempts we didn't successfully completed the transaction, we throw the last exception that caused the issue
        throw lastException.getCause();
    }

    private void sleep(final long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (final InterruptedException ie) {
            if (logger.isLoggable(JTATransactionServiceExt.class, TechnicalLogSeverity.TRACE)) {
                logger.log(JTATransactionServiceExt.class, TechnicalLogSeverity.TRACE, "Retry sleeping was interrupted!");
            }
        }
    }

}
