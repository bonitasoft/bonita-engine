/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface TenantServiceAccessor extends org.bonitasoft.engine.service.TenantServiceAccessor {

    ReportingService getReportingService();

    TenantMonitoringService getTenantMonitoringService();

    @Override
    SearchEntitiesDescriptor getSearchEntitiesDescriptor();

}
