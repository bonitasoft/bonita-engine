/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
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
