/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services;

import org.bonitasoft.engine.test.util.ServicesAccessor;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import com.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;

/**
 * You can set these system properties to change implementations
 * sysprop.bonita.db.vendor
 * change the DB:
 * h2 (default)
 * mysql
 * oracle
 * postgres
 * bonita.test.identity.service
 * change the identity service
 * bonita-identity-impl (default)
 * bonita.test.identity.model
 * change the identity model
 * bonita-identity-model-impl (default)
 * 
 * @author Celine Souchet
 */
public class ServicesBuilder extends org.bonitasoft.engine.ServicesBuilder {

    public ServicesBuilder() {
        super();
        setAccessor(ServicesAccessor.getInstance());
    }

    public SServiceMXBean getServiceMXBean() {
        return getAccessor().getInstanceOf(SServiceMXBean.class);
    }

    public SPlatformServiceMXBean getPlatformServiceMXBean() {
        return getAccessor().getInstanceOf(SPlatformServiceMXBean.class);
    }

    public SJvmMXBean getJvmMXBean() {
        return getAccessor().getInstanceOf(SJvmMXBean.class);
    }

    public TenantMonitoringService buildTenantMonitoringService() {
        return getAccessor().getInstanceOf("monitoringService", TenantMonitoringService.class);
    }

    public PlatformMonitoringService buildPlatformMonitoringService() {
        return getAccessor().getInstanceOf(PlatformMonitoringService.class);
    }
}
