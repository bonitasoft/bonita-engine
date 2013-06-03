/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;

/**
 * Add or remove token on the flow node instance
 * 
 * @author Elias Ricken de Medeiros
 */
public class AddActivityInstanceTokenCount implements TransactionContentWithResult<Integer> {

    private final ActivityInstanceService activityInstanceService;

    private final long activityInstanceId;

    private final int tokenToAdd;

    private int tokenCount;

    public AddActivityInstanceTokenCount(final ActivityInstanceService activityInstanceService, final long activityInstanceId, final int tokenToAdd) {
        this.activityInstanceService = activityInstanceService;
        this.activityInstanceId = activityInstanceId;
        this.tokenToAdd = tokenToAdd;
    }

    @Override
    public void execute() throws SBonitaException {
        final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
        tokenCount = activityInstance.getTokenCount() + tokenToAdd;
        activityInstanceService.setTokenCount(activityInstance, tokenCount);
    }

    @Override
    public Integer getResult() {
        return tokenCount;
    }

}
