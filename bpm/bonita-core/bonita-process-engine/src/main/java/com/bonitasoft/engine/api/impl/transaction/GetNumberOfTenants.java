/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
