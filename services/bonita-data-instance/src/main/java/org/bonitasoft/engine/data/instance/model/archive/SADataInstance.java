/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.data.instance.model.archive;

import java.io.Serializable;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "DISCRIMINANT")
@Table(name = "arch_data_instance")
public abstract class SADataInstance implements ArchivedPersistentObject {

    @Id
    private long tenantId;
    @Id
    private long id;
    private String name;
    private String description;
    private boolean transientData;
    private String className;
    private long containerId;
    private String containerType;
    private long archiveDate;
    private long sourceObjectId;

    public SADataInstance(final SDataInstance sDataInstance) {
        name = sDataInstance.getName();
        description = sDataInstance.getDescription();
        transientData = sDataInstance.isTransientData();
        className = sDataInstance.getClassName();
        containerId = sDataInstance.getContainerId();
        containerType = sDataInstance.getContainerType();
        sourceObjectId = sDataInstance.getId();
    }

    public abstract Serializable getValue();

    public abstract void setValue(Serializable value);

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDataInstance.class;
    }

}
