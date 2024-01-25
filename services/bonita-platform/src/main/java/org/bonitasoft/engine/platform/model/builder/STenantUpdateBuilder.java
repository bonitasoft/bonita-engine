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
package org.bonitasoft.engine.platform.model.builder;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public interface STenantUpdateBuilder {

    EntityUpdateDescriptor done();

    STenantUpdateBuilder setName(String name);

    STenantUpdateBuilder setDescription(String description);

    STenantUpdateBuilder setIconPath(String iconPath);

    STenantUpdateBuilder setIconName(String iconName);

    STenantUpdateBuilder setStatus(String status);

    STenantUpdateBuilder setSecurityActivated(boolean securityActivated);

}
