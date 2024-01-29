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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import java.io.Serializable;
import java.util.Date;

import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.impl.ArchivedDataInstanceImpl;

public class ArchivedDataInstanceBuilder {

    public static ArchivedDataInstanceBuilder anArchivedDataInstance(String name) {
        return new ArchivedDataInstanceBuilder(name);
    }

    private final String name;
    private String description;
    private String className;
    private Serializable value;
    private long containerId;
    private Date archivedDate;
    private long sourceObjectId;

    private ArchivedDataInstanceBuilder(String name) {
        this.name = name;
    }

    public ArchivedDataInstanceBuilder withType(String className) {
        this.className = className;
        return this;
    }

    public ArchivedDataInstanceBuilder withValue(Serializable value) {
        this.value = value;
        return this;
    }

    public ArchivedDataInstanceBuilder withContainerId(long containerId) {
        this.containerId = containerId;
        return this;
    }

    public ArchivedDataInstanceBuilder withArchivedDate(Date archivedDate) {
        this.archivedDate = archivedDate;
        return this;
    }

    public ArchivedDataInstance build() {
        var instance = new ArchivedDataInstanceImpl();
        instance.setName(name);
        instance.setClassName(className);
        instance.setContainerId(containerId);
        instance.setContainerType("PROCESS_INSTANCE");
        instance.setDescription(description);
        instance.setSourceObjectId(sourceObjectId);
        instance.setArchiveDate(archivedDate);
        instance.setValue(value);
        return instance;
    }
}
