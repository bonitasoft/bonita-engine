package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.SPlatformNotFoundException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.transaction.STransactionException;

public class AbstractLoginApiImpl {

    public AbstractLoginApiImpl() {
        super();
    }

    protected void putPlatformInCacheIfNecessary(PlatformServiceAccessor platformAccessor, PlatformService platformService) {
        // Get the platform from cache:
        try {
            platformService.getPlatform();
        } catch (SPlatformNotFoundException e1) {
            try {
                TransactionExecutor transactionExecutor = platformAccessor.getTransactionExecutor();
                boolean openTransaction = false;
                try {
                    openTransaction = transactionExecutor.openTransaction();
                    platformService.getPlatform();
                } catch (SPlatformNotFoundException e) {
                    platformAccessor.getTechnicalLoggerService().log(getClass(), TechnicalLogSeverity.INFO, "Platform not yet created");
                } finally {
                    transactionExecutor.completeTransaction(openTransaction);
                }
            } catch (STransactionException e) {
                throw new BonitaRuntimeException("Transaction error on getting platform at login", e);
            }
        }
    }

}
