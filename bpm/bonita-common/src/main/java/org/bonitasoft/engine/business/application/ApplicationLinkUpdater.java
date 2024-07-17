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
package org.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.Map;

/**
 * Allows to define which {@link ApplicationLink} fields will be updated
 *
 * @see ApplicationLink
 * @since 10.2.0
 * @deprecated This class should no longer be used. Since 9.0.0, Applications should be updated at startup.
 */
@Deprecated(since = "10.2.0")
public class ApplicationLinkUpdater extends AbstractApplicationUpdater<ApplicationLinkUpdater> {

    private static final long serialVersionUID = 1732835829535757371L;
    private transient Map<ApplicationField, Serializable> fieldsCheckedMap;

    @Override
    public Map<ApplicationField, Serializable> getFields() {
        // make sure no field for Legacy Application and not suitable for Application Link will get inserted there
        if (fieldsCheckedMap == null) {
            fieldsCheckedMap = new CheckedApplicationFieldMap(super.getFields(),
                    k -> k.isForClass(ApplicationLink.class));
        }
        return fieldsCheckedMap;
    }
}
