/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
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
