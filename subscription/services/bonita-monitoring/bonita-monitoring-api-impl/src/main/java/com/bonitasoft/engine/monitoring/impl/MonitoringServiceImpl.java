/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.bonitasoft.engine.monitoring.MonitoringService;
import com.bonitasoft.engine.monitoring.mbean.BonitaMXBean;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;

/**
 * @author Christophe Havard
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class MonitoringServiceImpl implements MonitoringService {

    private final Collection<BonitaMXBean> bonitaMXBeans = new ArrayList<BonitaMXBean>();

    private final boolean allowMbeansRegistration;

    protected final TechnicalLoggerService technicalLog;

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
