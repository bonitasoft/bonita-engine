/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 */
public class AbstractLoginApiImpl {

    public AbstractLoginApiImpl() {
        super();
    }

    protected void putPlatformInCacheIfNecessary(final PlatformServiceAccessor platformAccessor, final PlatformService platformService) {
        // Get the platform from cache:
        try {
            // First call that will look into the cache and tries to fetch it from the DB
            // if not in cache : but there is no transaction open yet so that will fail
            platformService.getPlatform();
        } catch (final SPlatformNotFoundException e1) {
            final TransactionService transactionService = platformAccessor.getTransactionService();
            try {
                transactionService.executeInTransaction(new Callable<SPlatform>() {
                    @Override
                    public SPlatform call() throws Exception {
                        try {
                            // Second call that will look into the cache and fetches it from the DB but this time the
                            return platformService.getPlatform();
                        } catch (final SPlatformNotFoundException e) {
                            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
                            if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
                                logger.log(getClass(), TechnicalLogSeverity.INFO, "Platform not yet created");
                            }
                            throw e;
                        }
                    }
                });
            } catch (Exception e) {
                throw new SBonitaRuntimeException(e);
            }
        }
    }

}
