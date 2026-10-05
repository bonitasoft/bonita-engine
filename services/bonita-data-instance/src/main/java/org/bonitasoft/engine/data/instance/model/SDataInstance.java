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

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.bonitasoft.engine.persistence.PersistentObject;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINANT")
@Table(name = "data_instance")
public abstract class SDataInstance implements PersistentObject {

    /**
     * BPA-833: pinned to the UID this class had <em>before</em> the hand-written <code>Boolean isTransientData()</code>
     * gave way to Lombok's primitive accessor. The default computation covers non-private methods, so dropping that
     * accessor moved the implicit UID and would stop a node running the new jar from deserializing an entry written
     * by a node running the old one - {@code SDataInstance} goes through {@code CacheService}, a Hazelcast
     * distributed map in cluster mode. Only methods changed, not fields, so the serialized form is identical and
     * keeping the old value restores interoperability.
     * <p>
     * Both values were measured with {@code serialver}, not derived by hand - a wrong pin is worse than none, since
     * it freezes the class at a UID it never had, and that is exactly the mistake this pin was corrected from.
     * -2379027971578351313 is the pre-change value, pinned below; -6317727528417777012 is what the class computes
     * with the accessor gone and no pin. The procedure for re-measuring is in
     * {@code doc/groovy_3_to_4-specific-study/}, since it names a merge-base commit that means nothing once this
     * merges.
     */
    @Serial
    private static final long serialVersionUID = -2379027971578351313L;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String VALUE = "value";
    public static final String CONTAINER_ID = "containerId";
    public static final String CONTAINER_TYPE = "containerType";
    @Id
    private long id;
    private String name;
    private String description;
    private boolean transientData;
    private String className;
    private long containerId;
    private String containerType;

    protected SDataInstance(final SDataDefinition dataDefinition) {
        name = dataDefinition.getName();
        description = dataDefinition.getDescription();
        transientData = dataDefinition.isTransientData();
        className = dataDefinition.getClassName();
    }

    public abstract void setValue(Serializable value);

    public abstract Serializable getValue();

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
