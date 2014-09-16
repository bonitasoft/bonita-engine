/*******************************************************************************
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean.impl;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.monitoring.SMonitoringException;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;

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

    private final TransactionService transactionSvc;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private long numberOfExecutingJobs = 0;

    public SServiceMXBeanImpl(final TransactionService transactionSvc, final TenantMonitoringService monitoringService, final SessionAccessor sessionAccessor,
            final SessionService sessionService) {
        this.monitoringService = monitoringService;
        this.sessionAccessor = sessionAccessor;
        mbserver = MBeanUtil.getMBeanServer();
        this.sessionService = sessionService;
        this.transactionSvc = transactionSvc;
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
    public long getNumberOfActiveTransactions() {
        return transactionSvc.getNumberOfActiveTransactions();
    }

    @Override
    public long getNumberOfExecutingJobs() throws SMonitoringException {
        long sessionId = -1;
        try {
            sessionId = MBeanUtil.createSession(transactionSvc, sessionAccessor, sessionService, tenantId, username);
            numberOfExecutingJobs = monitoringService.getNumberOfExecutingJobs();
        } catch (final Exception e) {
            throw new SMonitoringException("Impossible to retrieve number of executing jobs", e);
        } finally {
            if (sessionId != -1) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(sessionId);
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
