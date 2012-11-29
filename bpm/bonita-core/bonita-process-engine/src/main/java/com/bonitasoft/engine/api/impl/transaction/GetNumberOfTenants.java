/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.platform.PlatformService;

/**
 * @author Lu Kai
 */
public class GetNumberOfTenants implements TransactionContentWithResult<Integer> {

    private final PlatformService platformService;

    private int tenantCount;

    public GetNumberOfTenants(final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Override
    public void execute() throws SBonitaException {
        tenantCount = platformService.getNumberOfTenants();
    }

    @Override
    public Integer getResult() {
        return tenantCount;
    }

}
