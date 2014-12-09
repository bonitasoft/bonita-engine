/*******************************************************************************
 * Copyright (C) 2011,2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean.impl;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.monitoring.SMonitoringException;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import com.bonitasoft.engine.monitoring.mbean.SEntityMXBean;

/**
 * With this implementation, you can choose to use the local cache or a cluster configuration.
 * The local cache version improve the performance by..
 * 
 * @author Christophe Havard
 * @author Elias Ricken de Medeiros
 */
public class SEntityMXBeanImpl implements SEntityMXBean {

    private final MBeanServer mbserver;

    private ObjectName name;

    private String strName;

    private final TransactionService transactionSvc;

    private long tenantId;

    private String username;

    private final TenantMonitoringService monitoringService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    /**
     * this attributes contains the number of users. It is used only in the cache mode.
     * In a cluster configuration (useCache == false), the number of users is retrieved directly from the database,
     * through the IdentityService.
     * 
     * @return
     */
    public long numberOfUsers = 0;

    /**
     * Default constructor.
     * The useCache parameter is defined by configuration. If it is true, it means there is no
     * cluster configuration and the local memory is used.
     * 
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws TenantNotSetException
     */
    public SEntityMXBeanImpl(final TransactionService transactionSvc, final TenantMonitoringService monitoringService, final SessionAccessor sessionAccessor,
            final SessionService sessionService) {
        this.transactionSvc = transactionSvc;
        mbserver = MBeanUtil.getMBeanServer();
        numberOfUsers = 0;
        this.monitoringService = monitoringService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public void start() throws MBeanStartException {
        try {
            // set tenant
            tenantId = sessionAccessor.getTenantId();
            final long sessionId = sessionAccessor.getSessionId();
            final SSession session = sessionService.getSession(sessionId);
            username = session.getUserName();

            // set name
            strName = TenantMonitoringService.ENTITY_MBEAN_PREFIX + tenantId;
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
            // Unregister the MXBean
            if (name != null) {
                if (mbserver.isRegistered(name)) {
                    mbserver.unregisterMBean(name);
                }
            }
        } catch (final Exception e) {
            throw new MBeanStopException(e);
        }
    }

    @Override
    public long getNumberOfUsers() throws SMonitoringException {

        long sessionId = -1;
        try {
            sessionId = MBeanUtil.createSession(transactionSvc, sessionAccessor, sessionService, tenantId, username);
            transactionSvc.begin();
            numberOfUsers = monitoringService.getNumberOfUsers();

        } catch (final Exception e) {
            throw new SMonitoringException("Impossible to retrieve number of users", e);
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
            try {
                transactionSvc.complete();
            } catch (final SBonitaException e) {
                throw new SMonitoringException("Impossible to complete transaction", e);
            }
        }
        return numberOfUsers;
    }

    @Override
    public String getName() {
        return strName;
    }

}
