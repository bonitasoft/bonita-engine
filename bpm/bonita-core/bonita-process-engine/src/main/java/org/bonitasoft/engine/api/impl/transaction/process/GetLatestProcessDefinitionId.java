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
package org.bonitasoft.engine.api.impl.transaction.process;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;

/**
 * @author Yanyan Liu
 */
public class GetLatestProcessDefinitionId implements TransactionContentWithResult<Long> {

    private final ProcessDefinitionService processDefinitionService;

    private final String processName;

    private long processDefId;

    public GetLatestProcessDefinitionId(final ProcessDefinitionService processDefinitionService, final String processName) {
        this.processDefinitionService = processDefinitionService;
        this.processName = processName;
    }

    @Override
    public void execute() throws SBonitaException {
        this.processDefId = this.processDefinitionService.getLatestProcessDefinitionId(this.processName);
    }

    @Override
    public Long getResult() {
        return this.processDefId;
    }

}
