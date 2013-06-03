/**
 * Copyright (21 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;

/**
 * @author Yanyan Liu
 */
public class GetNumberOfDataInstanceForContainer implements TransactionContentWithResult<Long> {

    private final long containerId;

    private final DataInstanceContainer containerType;

    private final DataInstanceService dataInstanceService;

    private long count;

    public GetNumberOfDataInstanceForContainer(final long containerId, final DataInstanceContainer containerType, final DataInstanceService dataInstanceService) {
        this.containerId = containerId;
        this.containerType = containerType;
        this.dataInstanceService = dataInstanceService;
    }

    @Override
    public void execute() throws SBonitaException {
        this.count = this.dataInstanceService.getNumberOfDataInstances(this.containerId, this.containerType);
    }

    @Override
    public Long getResult() {
        return this.count;
    }

}
