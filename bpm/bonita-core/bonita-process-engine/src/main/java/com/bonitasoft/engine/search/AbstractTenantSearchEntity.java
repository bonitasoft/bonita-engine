/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchEntityDescriptor;
import org.bonitasoft.engine.search.SearchOptions;

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
