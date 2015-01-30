/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.service.impl.SpringPlatformFileSystemBeanAccessor;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.search.descriptor.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.BroadcastService;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SpringPlatformServiceAccessor extends org.bonitasoft.engine.service.impl.SpringPlatformServiceAccessor implements PlatformServiceAccessor {

    private PlatformMonitoringService platformMonitoringService;

    private SearchPlatformEntitiesDescriptor searchPlatformEntitiesDescriptor;

    private BroadcastService broadcastService;

    @Override
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        if (searchPlatformEntitiesDescriptor == null) {
            searchPlatformEntitiesDescriptor = SpringPlatformFileSystemBeanAccessor.getService(SearchPlatformEntitiesDescriptor.class);
        }
        return searchPlatformEntitiesDescriptor;
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        final TenantServiceAccessor instance = TenantServiceSingleton.getInstance(tenantId);
        return instance;
    }

    @Override
    public PlatformMonitoringService getPlatformMonitoringService() {
        if (platformMonitoringService == null) {
            platformMonitoringService = SpringPlatformFileSystemBeanAccessor.getService(PlatformMonitoringService.class);
        }
        return platformMonitoringService;
    }

    @Override
    public BroadcastService getBroadcastService() {
        if (broadcastService == null) {
            broadcastService = SpringPlatformFileSystemBeanAccessor.getService(BroadcastService.class);
        }
        return broadcastService;
    }
}
