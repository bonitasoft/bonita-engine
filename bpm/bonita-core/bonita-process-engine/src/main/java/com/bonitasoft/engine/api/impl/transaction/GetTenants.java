/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Baptiste Mesta
 */
public final class GetTenants implements TransactionContentWithResult<List<STenant>> {

    private final PlatformService platformService;

    private List<STenant> tenantList;

    public GetTenants(final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Override
    public void execute() throws SBonitaException {
        tenantList = platformService.getTenants(QueryOptions.allResultsQueryOptions());
    }

    @Override
    public List<STenant> getResult() {
        return tenantList;
    }

}
