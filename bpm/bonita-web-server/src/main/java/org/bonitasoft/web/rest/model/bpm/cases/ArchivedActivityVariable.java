/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.model.bpm.cases;

import java.io.Serializable;

import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;

public class ArchivedActivityVariable extends ArchivedVariable {

    /**
     * ID of the container this variable belongs to
     */
    private String containerId;

    /**
     * Type of the container this variable belongs to
     */
    private String containerType;

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public static ArchivedActivityVariable create(ArchivedDataInstance archivedProcessDataInstance) {
        var instance = new ArchivedActivityVariable();
        instance.setName(archivedProcessDataInstance.getName());
        instance.setContainerId(String.valueOf(archivedProcessDataInstance.getContainerId()));
        instance.setDescription(archivedProcessDataInstance.getDescription());
        instance.setType(archivedProcessDataInstance.getClassName());
        instance.setContainerType(archivedProcessDataInstance.getContainerType());
        Serializable value = archivedProcessDataInstance.getValue();
        instance.setValue(value == null ? null : String.valueOf(value));
        instance.setArchivedDate(archivedProcessDataInstance.getArchiveDate());
        archivedProcessDataInstance.getContainerType();
        instance.setSourceObjectId(String.valueOf(archivedProcessDataInstance.getSourceObjectId()));
        return instance;
    }

}
