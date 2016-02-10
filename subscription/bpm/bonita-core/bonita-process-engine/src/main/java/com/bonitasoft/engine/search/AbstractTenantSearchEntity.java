/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.service.SPModelConvertor;

/**
 * @author Zhao Na
 */
public abstract class AbstractTenantSearchEntity extends AbstractSearchEntity<Tenant, STenant> {

    public AbstractTenantSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
    }

    @Override
    public List<Tenant> convertToClientObjects(final List<STenant> serverObjects) {
        return SPModelConvertor.toTenants(serverObjects);
    }

}
