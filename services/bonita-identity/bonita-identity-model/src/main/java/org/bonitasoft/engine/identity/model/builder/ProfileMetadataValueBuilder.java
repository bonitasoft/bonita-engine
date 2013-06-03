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

import org.bonitasoft.engine.identity.model.SProfileMetadataValue;

/**
 * @author Baptiste Mesta
 * @aythor Matthieu Chaffotte
 */
public interface ProfileMetadataValueBuilder {

    ProfileMetadataValueBuilder createNewInstance();

    ProfileMetadataValueBuilder setUserName(final String userName);

    ProfileMetadataValueBuilder setMetadataName(final String metadataName);

    ProfileMetadataValueBuilder setValue(final String value);

    SProfileMetadataValue done();

    String getIdKey();

    String getUserNameKey();

    String getMetadataNameKey();

    String getValueKey();

}
