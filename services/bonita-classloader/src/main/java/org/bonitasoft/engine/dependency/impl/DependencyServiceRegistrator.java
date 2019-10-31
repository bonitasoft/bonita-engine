package org.bonitasoft.engine.dependency.impl;

import javax.annotation.PostConstruct;

import org.bonitasoft.engine.classloader.ClassLoaderService;

public class DependencyServiceRegistrator {

    private TenantDependencyService tenantDependencyService;
    private ClassLoaderService classLoaderService;
    private long tenantId;

    public DependencyServiceRegistrator(TenantDependencyService tenantDependencyService, ClassLoaderService classLoaderService, long tenantId) {
        this.tenantDependencyService = tenantDependencyService;
        this.classLoaderService = classLoaderService;
        this.tenantId = tenantId;
    }

    @PostConstruct
    private void registerDependencyService() {
        classLoaderService.registerDependencyServiceOfTenant(tenantId, tenantDependencyService);
    }
}
