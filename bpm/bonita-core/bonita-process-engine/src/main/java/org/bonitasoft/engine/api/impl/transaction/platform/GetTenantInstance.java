/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.platform;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Lu Kai
 * @author Baptiste Mesta
 */
public class GetTenantInstance implements TransactionContentWithResult<STenant> {

    private STenant sTenant;

    private final PlatformService platformService;

    private final String tenantName;

    private final long tenantId;

    public GetTenantInstance(final String tenantName, final PlatformService platformService) {
        this.tenantName = tenantName;
        this.platformService = platformService;
        tenantId = -1;
    }

    public GetTenantInstance(final long tenantId, final PlatformService platformService) {
        this.tenantId = tenantId;
        this.platformService = platformService;
        tenantName = null;
    }

    @Override
    public void execute() throws SBonitaException {
        if (tenantName == null) {
            sTenant = platformService.getTenant(tenantId);
        } else {
            sTenant = platformService.getTenantByName(tenantName);
        }
    }

    @Override
    public STenant getResult() {
        return sTenant;
    }

}
