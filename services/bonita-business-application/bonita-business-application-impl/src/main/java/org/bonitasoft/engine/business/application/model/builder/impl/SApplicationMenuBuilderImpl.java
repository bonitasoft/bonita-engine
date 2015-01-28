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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilder;
import org.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationMenuBuilderImpl implements SApplicationMenuBuilder {

    private final SApplicationMenuImpl applicationMenu;

    public SApplicationMenuBuilderImpl(final SApplicationMenuImpl applicationMenu) {
        this.applicationMenu = applicationMenu;
    }

    @Override
    public SApplicationMenu done() {
        return applicationMenu;
    }

    @Override
    public SApplicationMenuBuilder setParentId(final long parentId) {
        applicationMenu.setParentId(parentId);
        return this;
    }

}
