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
package org.bonitasoft.engine.data.instance.model.builder.impl;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SDataInstanceBuilderImpl implements SDataInstanceBuilder {

    private final SDataInstanceImpl dataInstanceImpl;

    public SDataInstanceBuilderImpl(final SDataInstanceImpl dataInstanceImpl) {
        super();
        this.dataInstanceImpl = dataInstanceImpl;
    }

    @Override
    public SDataInstanceBuilder setValue(final Serializable value) {
        dataInstanceImpl.setValue(value);
        return this;
    }

    @Override
    public SDataInstanceBuilder setContainerId(final long containerId) {
        dataInstanceImpl.setContainerId(containerId);
        return this;
    }

    @Override
    public SDataInstanceBuilder setContainerType(final String containerType) {
        dataInstanceImpl.setContainerType(containerType);
        return this;
    }

    @Override
    public SDataInstance done() throws SDataInstanceNotWellFormedException {
        dataInstanceImpl.validate();
        return dataInstanceImpl;
    }

}
