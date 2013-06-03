/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder;

import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;

/**
 * @author Baptiste Mesta
 */
public interface ProfileMetadataDefinitionBuilder {

    ProfileMetadataDefinitionBuilder createNewInstance();

    ProfileMetadataDefinitionBuilder setId(final long id);

    ProfileMetadataDefinitionBuilder setName(final String name);

    ProfileMetadataDefinitionBuilder setDisplayName(final String displayName);

    ProfileMetadataDefinitionBuilder setDescription(final String description);

    SProfileMetadataDefinition done();

    String getIdKey();

    String getNameKey();

    String getDisplayNameKey();

    String getDescriptionKey();

}
