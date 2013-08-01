package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.SPlatformNotFoundException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;

public class AbstractLoginApiImpl {

    public AbstractLoginApiImpl() {
        super();
    }

    protected void putPlatformInCacheIfNecessary(PlatformServiceAccessor platformAccessor, PlatformService platformService) {
        // Get the platform from cache:
        try {
            // First call that will look into the cache and tries to fetch it from the DB 
            // if not in cache : but there is no transaction open yet so that will fail
            platformService.getPlatform();
        } catch (SPlatformNotFoundException e1) {
            try {
                TransactionService transactionService = platformAccessor.getTransactionService();
                transactionService.begin();
                try {
                    // Second call that will look into the cache and fetches it from the DB but this time the 
                    platformService.getPlatform();
                } catch (SPlatformNotFoundException e) {
                    platformAccessor.getTechnicalLoggerService().log(getClass(), TechnicalLogSeverity.INFO, "Platform not yet created");
                } finally {
                    transactionService.complete();
                }
            } catch (STransactionException e) {
                throw new BonitaRuntimeException("Transaction error on getting platform at login", e);
            }
        }
    }

}
