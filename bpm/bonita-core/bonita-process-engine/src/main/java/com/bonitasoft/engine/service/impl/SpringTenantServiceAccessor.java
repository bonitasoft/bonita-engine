/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.parameter.ParameterService;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.search.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SpringTenantServiceAccessor extends org.bonitasoft.engine.service.impl.SpringTenantServiceAccessor implements TenantServiceAccessor {

    private ParameterService parameterService;

    private BreakpointService breakpointService;

    private SearchEntitiesDescriptor searchEntitiesDescriptor;

    private BPMInstanceBuilders bpmInstanceBuilders;

    public SpringTenantServiceAccessor(final Long tenantId) {
        super(tenantId);
    }

    @Override
    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = getBeanAccessor().getService(ParameterService.class);
        }
        return parameterService;
    }

    @Override
    public BreakpointService getBreakpointService() {
        if (breakpointService == null) {
            breakpointService = getBeanAccessor().getService(BreakpointService.class);
        }
        return breakpointService;
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        if (searchEntitiesDescriptor == null) {
            searchEntitiesDescriptor = getBeanAccessor().getService(SearchEntitiesDescriptor.class);
        }
        return searchEntitiesDescriptor;
    }

    @Override
    public BPMInstanceBuilders getBPMInstanceBuilders() {
        if (bpmInstanceBuilders == null) {
            bpmInstanceBuilders = getBeanAccessor().getService(BPMInstanceBuilders.class);
        }
        return bpmInstanceBuilders;
    }

}
