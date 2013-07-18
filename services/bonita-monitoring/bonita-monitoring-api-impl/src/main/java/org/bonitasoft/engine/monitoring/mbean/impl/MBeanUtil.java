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

    public static long createSesssion(final SessionAccessor sessionAccessor, final SessionService sessionService,
            final long tenantId, final String username) throws Exception {
        final SSession session = sessionService.createSession(tenantId, username);
        sessionAccessor.setSessionInfo(session.getId(), tenantId);
        return session.getId();
    }

}
