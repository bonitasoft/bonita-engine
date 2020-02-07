/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.test.util;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Baptiste Mesta, Yanyan Liu
 */
public class TestUtil {

    private static final String DEFAULT_USER_NAME = "install";

    private static final String DEFAULT_PASSWORD = "install";

    public static String getDefaultUserName() {
        return DEFAULT_USER_NAME;
    }

    public static String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    public static void startScheduler(final SchedulerService scheduler) throws Exception {
        if (!scheduler.isStarted()) {
            scheduler.start();
        }
    }

    public static void stopScheduler(final SchedulerService scheduler, final TransactionService txService)
            throws Exception {
        if (scheduler.isStarted() && !scheduler.isStopped()) {
            try {
                txService.executeInTransaction((Callable<Void>) () -> {
                    try {
                        scheduler.deleteJobs();
                    } catch (SSchedulerException e) {
                        if (!scheduler.getAllJobs().isEmpty()) {
                            throw e;
                        }
                    }
                    return null;
                });
            } catch (final STransactionException txException) {
                throw new SSchedulerException(txException);
            }
            scheduler.stop();
        }
    }

    // This method should disappear as well, with the Transaction refactoring.
    public static void closeTransactionIfOpen(final TransactionService txService) throws STransactionException {
        final TransactionState txState = txService.getState();
        if (txState == TransactionState.ROLLBACKONLY || txState == TransactionState.ACTIVE) {
            txService.complete();
        }
    }

    public static void createSessionOn(final SessionAccessor sessionAccessor, final SessionService sessionService,
            final long tenantId)
            throws SBonitaException {
        try {
            sessionService.deleteSession(sessionAccessor.getSessionId());
            sessionAccessor.deleteSessionId();
        } catch (final SessionIdNotSetException | SSessionNotFoundException e) {
            // do nothing
        }
        final SSession session = sessionService.createSession(tenantId, DEFAULT_USER_NAME);
        sessionAccessor.setSessionInfo(session.getId(), tenantId);

    }

}
