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
 * @author Matthieu Chaffotte
 */
public class IsTenantEnable implements TransactionContentWithResult<Boolean> {

    private final PlatformService platformService;

    private final Long tenantId;

    private Boolean enable;

    public IsTenantEnable(final PlatformService platformService, final Long tenantId) {
        super();
        this.platformService = platformService;
        this.tenantId = tenantId;
    }

    @Override
    public void execute() throws SBonitaException {
        final STenant tenant = platformService.getTenant(tenantId);
        enable = STenant.ACTIVATED.equals(tenant.getStatus());
    }

    @Override
    public Boolean getResult() {
        return enable;
    }

}
