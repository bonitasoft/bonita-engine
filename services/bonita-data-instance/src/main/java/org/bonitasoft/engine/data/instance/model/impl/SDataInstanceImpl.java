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
package org.bonitasoft.engine.data.instance.model.impl;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;

/**
 * @author Zhao Na
 * @author Frederic Bouquet
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
public abstract class SDataInstanceImpl implements SDataInstance {

    private long tenantId;
    private long id;
    private String name;
    private String description;
    private boolean transientData;
    private String className;
    private long containerId;
    private String containerType;

    public SDataInstanceImpl(final SDataDefinition dataDefinition) {
        name = dataDefinition.getName();
        description = dataDefinition.getDescription();
        transientData = dataDefinition.isTransientData();
        className = dataDefinition.getClassName();
    }

    public abstract void setValue(Serializable value);

    @Override
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
