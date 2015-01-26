/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

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

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface TenantServiceAccessor extends org.bonitasoft.engine.service.TenantServiceAccessor {

    BreakpointService getBreakpointService();

    ReportingService getReportingService();

    PageService getPageService();

    TenantMonitoringService getTenantMonitoringService();

    @Override
    SearchEntitiesDescriptor getSearchEntitiesDescriptor();

    BusinessDataRepository getBusinessDataRepository();

    BusinessDataService getBusinessDataService();

    BusinessDataModelRepository getBusinessDataModelRepository();

    RefBusinessDataService getRefBusinessDataService();

    ApplicationService getApplicationService();

}
