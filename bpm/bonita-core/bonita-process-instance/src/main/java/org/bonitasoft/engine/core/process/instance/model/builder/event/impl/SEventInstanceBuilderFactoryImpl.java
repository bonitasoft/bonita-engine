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
package org.bonitasoft.engine.core.process.instance.model.builder.event.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.SEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.impl.SFlowNodeInstanceBuilderFactoryImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public abstract class SEventInstanceBuilderFactoryImpl extends SFlowNodeInstanceBuilderFactoryImpl
        implements SEventInstanceBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

}
