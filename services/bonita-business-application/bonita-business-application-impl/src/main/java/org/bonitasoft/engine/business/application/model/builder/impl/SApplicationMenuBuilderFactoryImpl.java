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

import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilderFactory;
import org.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationMenuBuilderFactoryImpl implements SApplicationMenuBuilderFactory {

    @Override
    public SApplicationMenuBuilder createNewInstance(final String displayName, long applicationId, final Long applicationPageId, final int index) {
        return new SApplicationMenuBuilderImpl(new SApplicationMenuImpl(displayName, applicationId, applicationPageId, index));
    }

    @Override
    public String getIdKey() {
        return SApplicationMenuFields.ID;
    }

    @Override
    public String getDisplayNameKey() {
        return SApplicationMenuFields.DISPLAY_NAME;
    }

    @Override
    public String getApplicationIdKey() {
        return SApplicationMenuFields.APPLICAITON_ID;
    }

    @Override
    public String getApplicationPageIdKey() {
        return SApplicationMenuFields.APPLICATION_PAGE_ID;
    }

    @Override
    public String getParentIdKey() {
        return SApplicationMenuFields.PARENT_ID;
    }

    @Override
    public String getIndexKey() {
        return SApplicationMenuFields.INDEX;
    }


}
