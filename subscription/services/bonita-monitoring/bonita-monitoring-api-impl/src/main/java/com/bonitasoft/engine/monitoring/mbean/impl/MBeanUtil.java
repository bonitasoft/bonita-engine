/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public class MBeanUtil {

    private static MBeanServer mbserver = null;

    public static MBeanServer getMBeanServer() {
        if (mbserver == null) {
            final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
            if (mbservers.size() > 0) {
                mbserver = mbservers.get(0);
            }
            if (mbserver != null) {
                // FIXME use logger
                // ("Bonitasoft " + mbserver.toString() + " MBeanServer has been found...");
            } else {
                mbserver = MBeanServerFactory.createMBeanServer();
            }
        }
        return mbserver;
    }

    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactory.getMemoryMXBean();
    }

    public static OperatingSystemMXBean getOSMXBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    public static ThreadMXBean getThreadMXBean() {
        return ManagementFactory.getThreadMXBean();
    }

    public static long createSession(final TransactionService transactionSvc, final SessionAccessor sessionAccessor, final SessionService sessionService,
            final long tenantId, final String username) throws Exception {
        SSession session = null;
        try {
            transactionSvc.begin();
            session = sessionService.createSession(tenantId, username);
            sessionAccessor.setSessionInfo(session.getId(), tenantId);

        } catch (final Exception e) {
            transactionSvc.setRollbackOnly();
            throw e;
        } finally {
            transactionSvc.complete();
        }
        return session.getId();
    }

}
