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

import java.io.Serializable;

/**
 * represents a user contact data
 *
 * @author Matthieu Chaffotte
 * @since 6.0.0
 */
public interface ContactData extends Serializable {

    /**
     * @return the user id of this contact data
     */
    long getUserId();

    /**
     * @return true if this contact data is some personal information
     */
    boolean isPersonal();

    /**
     * @return the contact email
     */
    String getEmail();

    /**
     * @return the contact phone number
     */
    String getPhoneNumber();

    /**
     * @return the contact mobile phone number
     */
    String getMobileNumber();

    /**
     * @return the contact fax number
     */
    String getFaxNumber();

    /**
     * @return the contact building
     */
    String getBuilding();

    /**
     * @return the contact room
     */
    String getRoom();

    /**
     * @return the contact address
     */
    String getAddress();

    /**
     * @return the contact ZIP code
     */
    String getZipCode();

    /**
     * @return the contact city
     */
    String getCity();

    /**
     * @return the contact state
     */
    String getState();

    /**
     * @return the contact country
     */
    String getCountry();

    /**
     * @return the contact web site address
     */
    String getWebsite();

}
