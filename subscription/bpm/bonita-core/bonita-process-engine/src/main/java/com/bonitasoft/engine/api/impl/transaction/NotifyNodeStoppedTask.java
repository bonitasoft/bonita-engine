/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import java.io.Serializable;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.GetTenantsCallable;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.service.BroadCastedTask;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
 */
public class NotifyNodeStoppedTask implements BroadCastedTask<Void>, Serializable {

    private static final long serialVersionUID = 7880459346729952396L;

    private String nodeName;

    public NotifyNodeStoppedTask() {
    }

    @Override
    public Void call() throws Exception {
        PlatformServiceAccessor createPlatformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        PlatformService platformService = createPlatformServiceAccessor.getPlatformService();
        TransactionService transactionService = createPlatformServiceAccessor.getTransactionService();
        List<STenant> tenants = transactionService.executeInTransaction(new GetTenantsCallable(platformService));
        for (STenant sTenant : tenants) {
            TenantServiceSingleton.getInstance(sTenant.getId()).getWorkService().notifyNodeStopped(nodeName);
        }
        return null;
    }

    @Override
    public void setName(final String nodeName) {
        this.nodeName = nodeName;
    }

}
