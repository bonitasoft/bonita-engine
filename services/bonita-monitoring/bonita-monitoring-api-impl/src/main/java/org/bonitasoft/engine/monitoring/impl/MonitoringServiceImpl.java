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
package org.bonitasoft.engine.monitoring.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.MonitoringService;
import org.bonitasoft.engine.monitoring.mbean.BonitaMXBean;
import org.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStopException;

/**
 * @author Christophe Havard
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class MonitoringServiceImpl implements MonitoringService {

    private final Collection<BonitaMXBean> bonitaMXBeans = new ArrayList<BonitaMXBean>();

    private final boolean allowMbeansRegistration;

    private final TechnicalLoggerService technicalLog;

    public MonitoringServiceImpl(final boolean allowMbeans, final TechnicalLoggerService technicalLog) {
        allowMbeansRegistration = allowMbeans;
        this.technicalLog = technicalLog;
    }

    @Override
    public void addMBean(final BonitaMXBean bonitaMBean) {
        bonitaMXBeans.add(bonitaMBean);
    }

    @Override
    public void registerMBeans() throws MBeanStartException {
        if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            technicalLog.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "registerMBeans"));
        }
        if (allowMbeansRegistration) {
            for (final BonitaMXBean mb : bonitaMXBeans) {
                try {
                    mb.start();
                } catch (final Exception e) {
                    if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                        technicalLog.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "registerMBeans", e));
                    }
                    throw new MBeanStartException(e);
                }
            }
        } else {
            if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                technicalLog.log(this.getClass(), TechnicalLogSeverity.WARNING, "Unable to register MBeans because this function is disabled");
            }
        }
        if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            technicalLog.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "registerMBeans"));
        }
    }

    @Override
    public void unregisterMbeans() throws MBeanStopException {
        if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            technicalLog.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "unregisterMbeans"));
        }
        if (allowMbeansRegistration) {
            for (final BonitaMXBean mb : bonitaMXBeans) {
                try {
                    mb.stop();
                } catch (final Exception e) {
                    if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                        technicalLog.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "unregisterMbeans", e));
                    }
                    throw new MBeanStopException(e);
                }
            }
        }
        if (technicalLog.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            technicalLog.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "unregisterMbeans"));
        }
    }

}
