package org.bonitasoft.engine.bpm.test;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.service.impl.SessionAccessorAccessor;

import com.bonitasoft.engine.search.SearchEntitiesDescriptor;
import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;
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
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        return this.getInstanceOf(SearchPlatformEntitiesDescriptor.class);
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        return this.getInstanceOf(SearchEntitiesDescriptor.class);
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return this;
    }
}
