/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.test;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.service.impl.SessionAccessorAccessor;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.search.descriptor.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class SPBPMServicesBuilder extends BPMServicesBuilder implements PlatformServiceAccessor, TenantServiceAccessor, SessionAccessorAccessor {

    public SPBPMServicesBuilder() {
        super();
    }

    public SPBPMServicesBuilder(final Long tenantid) {
        super(tenantid);
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
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        return this.getInstanceOf(SearchPlatformEntitiesDescriptor.class);
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        return this.getInstanceOf(SearchEntitiesDescriptor.class);
    }

    @Override
    public BPMInstanceBuilders getBPMInstanceBuilders() {
        return this.getInstanceOf(BPMInstanceBuilders.class);
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return this;
    }
}
