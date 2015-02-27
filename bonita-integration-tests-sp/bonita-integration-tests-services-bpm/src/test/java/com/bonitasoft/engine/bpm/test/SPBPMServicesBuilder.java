/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.test;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.parameter.ParameterService;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import com.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.search.descriptor.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class SPBPMServicesBuilder extends BPMServicesBuilder implements PlatformServiceAccessor, TenantServiceAccessor {

    public SPBPMServicesBuilder() {
        super();
    }

    public SPBPMServicesBuilder(final Long tenantId) {
        super(tenantId);
    }

    @Override
    public ParameterService getParameterService() {
        return this.getInstanceOf(ParameterService.class);
    }

    @Override
    public BreakpointService getBreakpointService() {
        return this.getInstanceOf(BreakpointService.class);
    }

    @Override
    public ReportingService getReportingService() {
        return this.getInstanceOf(ReportingService.class);
    }

    @Override
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        return this.getInstanceOf(SearchPlatformEntitiesDescriptor.class);
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        return this.getInstanceOf(SearchEntitiesDescriptor.class);
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        // What is the parameter useful for ?
        return this;
    }

    @Override
    public PlatformMonitoringService getPlatformMonitoringService() {
        return getInstanceOf(PlatformMonitoringService.class);
    }

    @Override
    public TenantMonitoringService getTenantMonitoringService() {
        return getInstanceOf(TenantMonitoringService.class);
    }

    public SServiceMXBean getServiceMXBean() {
        return getInstanceOf(SServiceMXBean.class);
    }

    public SPlatformServiceMXBean getPlatformServiceMXBean() {
        return getInstanceOf(SPlatformServiceMXBean.class);
    }

    public SJvmMXBean getJvmMXBean() {
        return getInstanceOf(SJvmMXBean.class);
    }

    public TenantMonitoringService getTenantMonitoringService(final boolean useCache) {
        if (useCache) {
            return getInstanceOf("monitoringServiceWithCache", TenantMonitoringService.class);
        }
        return getInstanceOf("monitoringService", TenantMonitoringService.class);
    }

}
