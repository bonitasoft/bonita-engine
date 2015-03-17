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
package org.bonitasoft.engine.execution;

import java.io.IOException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Charles Souillard
 */
public abstract class AbstractSetInFailedThread extends Thread {

    // parameters build in the constructor
    private final TransactionExecutor txExecutor;

    private final long tenantId;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private final PlatformServiceAccessor platformServiceAccessor;

    private final TenantServiceAccessor tenantServiceAccessor;

    // output parameters
    private Throwable throwable;

    private boolean finished = false;

    public AbstractSetInFailedThread() throws STenantIdNotSetException, BonitaHomeNotSetException, BonitaHomeConfigurationException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, IOException {
        super();
        final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
        platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
        sessionAccessor = serviceAccessorFactory.createSessionAccessor();
        tenantId = sessionAccessor.getTenantId();
        tenantServiceAccessor = TenantServiceSingleton.getInstance(tenantId);
        txExecutor = tenantServiceAccessor.getTransactionExecutor();
        sessionService = platformServiceAccessor.getSessionService();
    }

    @Override
    public void run() {
        SSession session = null;
        try {
            // execute logic
            final TransactionContentWithResult<SSession> transactionContent = new TransactionContentWithResult<SSession>() {

                private SSession session = null;

                @Override
                public void execute() throws SBonitaException {
                    // create session
                    session = sessionService.createSession(tenantId, AbstractSetInFailedThread.class.getSimpleName() + " with id " + getId());// FIXME get the
                    sessionAccessor.setSessionInfo(session.getId(), tenantId);
                    setInFail();
                }

                @Override
                public SSession getResult() {
                    return session;
                }
            };
            txExecutor.execute(transactionContent);
            session = transactionContent.getResult();
        } catch (final Throwable t) {
            throwable = t;
        } finally {
            finished = true;
            // close session
            if (session != null) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(session.getId());
                } catch (final SSessionNotFoundException e) {
                    // ignore session deletion exception when session is not found
                }
            }
        }

    }

    protected abstract void setInFail() throws SBonitaException;

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isFinished() {
        return finished;
    }

    public PlatformServiceAccessor getPlatformServiceAccessor() {
        return platformServiceAccessor;
    }

    public TenantServiceAccessor getTenantServiceAccessor() {
        return tenantServiceAccessor;
    }

    public SessionAccessor getSessionAccessor() {
        return sessionAccessor;
    }

    public SessionService getSessionService() {
        return sessionService;
    }
}
