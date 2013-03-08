/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Yanyan Liu
 */
public class UpdateTenant implements TransactionContent {

    private final PlatformService platformService;

    private final EntityUpdateDescriptor changeDescriptor;

    private final long tenantId;

    public UpdateTenant(final long tenantId, final EntityUpdateDescriptor changeDescriptor, final PlatformService platformService) {
        this.tenantId = tenantId;
        this.platformService = platformService;
        this.changeDescriptor = changeDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final STenant tenant = platformService.getTenant(tenantId);
        platformService.updateTenant(tenant, changeDescriptor);
    }

}
