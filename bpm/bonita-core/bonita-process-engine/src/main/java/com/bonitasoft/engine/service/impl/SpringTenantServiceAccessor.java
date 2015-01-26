/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.data.BusinessDataModelRepository;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.BusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class SpringTenantServiceAccessor extends org.bonitasoft.engine.service.impl.SpringTenantServiceAccessor implements TenantServiceAccessor {

    private ParameterService parameterService;

    private BreakpointService breakpointService;

    private ReportingService reportingService;

    private TenantMonitoringService tenantMonitoringServie;

    private SearchEntitiesDescriptor searchEntitiesDescriptor;

    private PageService pageService;

    private BusinessDataRepository businessDataRespository;

    private RefBusinessDataService refBusinessDataService;

    private BusinessDataModelRepository businessDataModelRespository;

    private ApplicationService applicationService;

    private BusinessDataService businessDataService;

    public SpringTenantServiceAccessor(final Long tenantId) {
        super(tenantId);
    }

    private <T> T lookupService(final Class<T> clazz) {
        return getBeanAccessor().getService(clazz);
    }

    @Override
    public BreakpointService getBreakpointService() {
        if (breakpointService == null) {
            breakpointService = lookupService(BreakpointService.class);
        }
        return breakpointService;
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        if (searchEntitiesDescriptor == null) {
            searchEntitiesDescriptor = lookupService(SearchEntitiesDescriptor.class);
        }
        return searchEntitiesDescriptor;
    }

    @Override
    public ReportingService getReportingService() {
        if (reportingService == null) {
            reportingService = lookupService(ReportingService.class);
        }
        return reportingService;
    }

    @Override
    public TenantMonitoringService getTenantMonitoringService() {
        if (tenantMonitoringServie == null) {
            tenantMonitoringServie = lookupService(TenantMonitoringService.class);
        }
        return tenantMonitoringServie;
    }

    @Override
    public BusinessDataRepository getBusinessDataRepository() {
        if (businessDataRespository == null) {
            businessDataRespository = lookupService(BusinessDataRepository.class);
        }
        return businessDataRespository;
    }

    @Override
    public BusinessDataModelRepository getBusinessDataModelRepository() {
        if (businessDataModelRespository == null) {
            businessDataModelRespository = lookupService(BusinessDataModelRepository.class);
        }
        return businessDataModelRespository;
    }

    /**
     * might not be an available service
     */
    @Override
    public PageService getPageService() {
        if (pageService == null) {
            pageService = lookupService(PageService.class);
        }
        return pageService;
    }

    @Override
    public RefBusinessDataService getRefBusinessDataService() {
        if (refBusinessDataService == null) {
            refBusinessDataService = lookupService(RefBusinessDataService.class);
        }
        return refBusinessDataService;
    }

    @Override
    public ApplicationService getApplicationService() {
        if (applicationService == null) {
            applicationService = lookupService(ApplicationService.class);
        }
        return applicationService;
    }

    @Override
    public BusinessDataService getBusinessDataService() {
        if (businessDataService == null) {
            businessDataService = lookupService(BusinessDataService.class);
        }
        return businessDataService;
    }

}
