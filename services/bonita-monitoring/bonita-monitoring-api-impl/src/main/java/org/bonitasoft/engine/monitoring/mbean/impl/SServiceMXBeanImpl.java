/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.monitoring.mbean.impl;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.bonitasoft.engine.monitoring.SMonitoringException;
import org.bonitasoft.engine.monitoring.TenantMonitoringService;
import org.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import org.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Christophe Havard
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SServiceMXBeanImpl implements SServiceMXBean {

    private final MBeanServer mbserver;

    private ObjectName name;

    private String strName;

    private long tenantId;

    private String username;

    private final TenantMonitoringService monitoringService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private long numberOfactiveTransactions = 0;

    private long numberOfExecutingJobs = 0;

    public SServiceMXBeanImpl(final TenantMonitoringService monitoringService, final SessionAccessor sessionAccessor,
            final SessionService sessionService) {
        this.monitoringService = monitoringService;
        this.sessionAccessor = sessionAccessor;
        mbserver = MBeanUtil.getMBeanServer();
        this.sessionService = sessionService;
    }

    @Override
    public void start() throws MBeanStartException {

        try {
            // set tenant
            tenantId = sessionAccessor.getTenantId();

            // set username
            final long sessionId = sessionAccessor.getSessionId();
            final SSession session = sessionService.getSession(sessionId);
            username = session.getUserName();

            // set name
            strName = TenantMonitoringService.SERVICE_MBEAN_PREFIX + tenantId;
            name = new ObjectName(strName);

            // register the MXBean
            if (!mbserver.isRegistered(name)) {
                mbserver.registerMBean(this, name);
            }
        } catch (final Exception e) {
            throw new MBeanStartException(e);
        }
    }

    @Override
    public void stop() throws MBeanStopException {
        try {
            if (name != null) {
                // Unregister the MXBean
                if (mbserver.isRegistered(name)) {
                    mbserver.unregisterMBean(name);
                }
            }
        } catch (final Exception e) {
            throw new MBeanStopException(e);
        }
    }

    @Override
    public long getNumberOfActiveTransactions() throws SMonitoringException {
        long sesssionId = -1;
        try {
            sesssionId = MBeanUtil.createSesssion(sessionAccessor, sessionService, tenantId, username);
            numberOfactiveTransactions = monitoringService.getNumberOfActiveTransactions();
        } catch (final Exception e) {
            throw new SMonitoringException("Impossible to retrieve number of active transactions", e);
        } finally {
            if (sesssionId != -1) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(sesssionId);
                } catch (final SSessionNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return numberOfactiveTransactions;

    }

    @Override
    public long getNumberOfExecutingJobs() throws SMonitoringException {
        long sesssionId = -1;
        try {
            sesssionId = MBeanUtil.createSesssion(sessionAccessor, sessionService, tenantId, username);
            numberOfExecutingJobs = monitoringService.getNumberOfExecutingJobs();
        } catch (final Exception e) {
            throw new SMonitoringException("Impossible to retrieve number of executing jobs", e);
        } finally {
            if (sesssionId != -1) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(sesssionId);
                } catch (final SSessionNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return numberOfExecutingJobs;
    }

    @Override
    public String getName() {
        return strName;
    }

}
