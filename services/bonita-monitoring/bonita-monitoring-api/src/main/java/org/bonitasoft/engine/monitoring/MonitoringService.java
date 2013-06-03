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
package org.bonitasoft.engine.monitoring;

import org.bonitasoft.engine.monitoring.mbean.BonitaMXBean;
import org.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStopException;

/**
 * Start Monitoring MBeans and needed handlers
 * 
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public interface MonitoringService {

    /**
     * Starts the monitoring service : makes available the following MBeans :
     * <ul>
     * <li>Bonitasoft:name=Entity,type=EntityMBean</li>
     * <li>Bonitasoft:name=Service,type=ServiceMBean</li>
     * <li>Bonitasoft:name=JVM,type=JVMMBean</li>
     * </ul>
     * 
     * @throws MBeanStartException
     * @since 6.0
     */
    void registerMBeans() throws MBeanStartException;

    /**
     * Stops the monitoring service i.e makes the following MBeans unavailable.
     * <ul>
     * <li>Bonitasoft:name=Entity,type=EntityMBean</li>
     * <li>Bonitasoft:name=Service,type=ServiceMBean</li>
     * <li>Bonitasoft:name=JVM,type=JVMMBean</li>
     * </ul>
     * 
     * @since 6.0
     */
    void unregisterMbeans() throws MBeanStopException;

    /**
     * Add a bonitaMBean to the default collection of bonitaMXBeans
     * 
     * @param bonitaMBean
     *            BonitaMXBean
     * @since 6.0
     */
    void addMBean(BonitaMXBean bonitaMBean);

}
