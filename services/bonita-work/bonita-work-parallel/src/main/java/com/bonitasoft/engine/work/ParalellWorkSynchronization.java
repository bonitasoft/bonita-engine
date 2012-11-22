package com.bonitasoft.engine.work;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.AbstractWorkSynchronization;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class ParalellWorkSynchronization extends AbstractWorkSynchronization {

    public ParalellWorkSynchronization(final ThreadPoolExecutor threadPoolExecutor, final TechnicalLoggerService loggerService,
            final SessionAccessor sessionAccessor, final SessionService sessionService, final TransactionService transactionService) {
        super(threadPoolExecutor, loggerService, sessionAccessor, sessionService, transactionService);
    }

    @Override
    protected void executeRunnables(final Collection<BonitaWork> works) {
        for (final BonitaWork work : works) {
            threadPoolExecutor.submit(work);
        }
    }
}
