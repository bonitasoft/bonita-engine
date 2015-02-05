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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;

/**
 * @author Baptiste Mesta
 */
public class SConnectorInstanceBuilderImpl implements SConnectorInstanceBuilder {

    private final SConnectorInstanceImpl entity;

    public SConnectorInstanceBuilderImpl(final SConnectorInstanceImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SConnectorInstanceBuilder setState(final String state) {
        entity.setState(state);
        return this;
    }

    @Override
    public SConnectorInstance done() {
        return entity;
    }

}
