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
package org.bonitasoft.engine.identity.model.builder;

import org.bonitasoft.engine.identity.model.SContactInfo;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public interface SContactInfoBuilderFactory {

    SContactInfoBuilder createNewInstance(final long userId, final boolean isPersonal);

    SContactInfoBuilder createNewInstance(final SContactInfo contactInfo);

    String getIdKey();

    String getEmailKey();

    String getPhoneNumberKey();

    String getMobileNumberKey();

    String getFaxNumberKey();

    String getBuildingKey();

    String getRoomKey();

    String getAddressKey();

    String getZipCodeKey();

    String getCityKey();

    String getStateKey();

    String getCountryKey();

    String getWebsiteKey();

    String getIsPersonalKey();

}
