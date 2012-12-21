package org.bonitasoft.engine.bpm.test;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.SessionAccessorAccessor;

import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;

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
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        return this.getInstanceOf(SearchPlatformEntitiesDescriptor.class);
    }

}
