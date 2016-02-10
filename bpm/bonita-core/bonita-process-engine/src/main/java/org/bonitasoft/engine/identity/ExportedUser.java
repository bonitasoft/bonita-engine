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
package org.bonitasoft.engine.identity;

import java.util.List;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public interface ExportedUser {

    long getId();

    boolean isPasswordEncrypted();

    String getPassword();

    String getFirstName();

    String getLastName();

    String getUserName();

    String getIconName();

    String getIconPath();

    String getTitle();

    String getJobTitle();

    long getCreatedBy();

    long getManagerUserId();

    boolean isEnabled();

    /**
     * FIXME Remove ASAP
     */
    @Deprecated
    String getManagerUserName();

    // Personal info
    String getPersonalEmail();

    String getPersonalPhoneNumber();

    String getPersonalMobileNumber();

    String getPersonalFaxNumber();

    String getPersonalBuilding();

    String getPersonalRoom();

    String getPersonalAddress();

    String getPersonalZipCode();

    String getPersonalCity();

    String getPersonalState();

    String getPersonalCountry();

    String getPersonalWebsite();

    // Professional info
    String getProfessionalEmail();

    String getProfessionalPhoneNumber();

    String getProfessionalMobileNumber();

    String getProfessionalFaxNumber();

    String getProfessionalBuilding();

    String getProfessionalRoom();

    String getProfessionalAddress();

    String getProfessionalZipCode();

    String getProfessionalCity();

    String getProfessionalState();

    String getProfessionalCountry();

    String getProfessionalWebsite();
    
    List<ExportedCustomUserInfoValue> getCustomUserInfoValues();

}
