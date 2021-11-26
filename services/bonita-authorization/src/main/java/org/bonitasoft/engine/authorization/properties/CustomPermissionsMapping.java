/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.authorization.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Anthony Birembaut
 */
@Component
public class CustomPermissionsMapping extends ConfigurationFile {

    /**
     * Default name of the preferences file
     */
    public static final String PROPERTIES_FILENAME = "custom-permissions-mapping.properties";

    public CustomPermissionsMapping(@Value("${tenantId}") long tenantId) {
        super(PROPERTIES_FILENAME, tenantId);
    }

    @Override
    protected boolean hasCustomVersion() {
        return false;
    }

    @Override
    protected boolean hasInternalVersion() {
        return false;
    }
}
