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
package org.bonitasoft.engine.data.instance.model;

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
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "DISCRIMINANT")
@Table(name = "data_instance")
public abstract class SDataInstance implements PersistentObject {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String VALUE = "value";
    public static final String CONTAINER_ID = "containerId";
    public static final String CONTAINER_TYPE = "containerType";
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

    public SDataInstance(final SDataDefinition dataDefinition) {
        name = dataDefinition.getName();
        description = dataDefinition.getDescription();
        transientData = dataDefinition.isTransientData();
        className = dataDefinition.getClassName();
    }

    public abstract void setValue(Serializable value);

    public abstract Serializable getValue();

    public Boolean isTransientData() {
        return transientData;
    }

    public void setDataTypeClassName(final String className) {
        this.className = className;
    }

    /**
     * Check if the data is well formed
     *
     * @throws org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException
     *         thrown if the data is not well formed
     */
    public void validate() throws SDataInstanceNotWellFormedException {
    }

}
