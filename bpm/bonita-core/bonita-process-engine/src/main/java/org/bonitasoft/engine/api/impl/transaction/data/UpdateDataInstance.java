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
package org.bonitasoft.engine.api.impl.transaction.data;

import java.io.Serializable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Feng Hui
 */
public class UpdateDataInstance implements TransactionContent {

    private final long containerId;

    private final DataInstanceService dataInstanceService;

    private SDataInstance sDataInstance;

    private final String dataName;

    private final Serializable dataValue;

    private final String containerType;

    public UpdateDataInstance(final String dataName, final long containerId, final String containerType, final Serializable dataValue,
            final DataInstanceService dataInstanceService) {
        this.containerId = containerId;
        this.dataInstanceService = dataInstanceService;
        this.dataName = dataName;
        this.dataValue = dataValue;
        this.containerType = containerType;
    }

    @Override
    public void execute() throws SBonitaException {
        sDataInstance = dataInstanceService.getDataInstance(dataName, containerId, containerType);
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField("value", dataValue);
        dataInstanceService.updateDataInstance(sDataInstance, entityUpdateDescriptor);
    }

}
