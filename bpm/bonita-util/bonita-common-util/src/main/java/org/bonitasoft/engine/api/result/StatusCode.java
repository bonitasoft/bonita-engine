/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.result;

public enum StatusCode {

    /**
     * Everything is fine
     */
    OK,

    /**
     * Detected a duplication of attributes in a BDM Access Control rule
     */
    BDM_ACCESS_CONTROL_ATTRIBUTES_DUPLICATION_ERROR,

    /**
     * Detected a duplication of static profiles in a BDM Access Control rule
     */
    BDM_ACCESS_CONTROL_STATIC_PROFILES_DUPLICATION_ERROR,

    /**
     * Detected a duplication of rule name in a BDM Access Control set of Business Object rules
     */
    BDM_ACCESS_CONTROL_RULE_NAME_DUPLICATION_ERROR,

    /**
     * BDM Access Control: Several set of rules apply to the same Business Object. Solution: centralize them in the same set of rules
     */
    BDM_ACCESS_CONTROL_BUSINESS_OBJECT_REFERENCE_DUPLICATION_ERROR,

    /**
     * A BDM Access Control rule refers to an unknown Business Object attribute
     */
    BDM_ACCESS_CONTROL_UNKNOWN_ATTRIBUTE_REFERENCE,

    /**
     * A BDM Access Control rule refers to an unknown Business Object
     */
    BDM_ACCESS_CONTROL_UNKNOWN_BUSINESS_OBJECT_REFERENCE,

    /**
     * A BDM Access Control rule refers to an unknown profile
     */
    BDM_ACCESS_CONTROL_UNKNOWN_PROFILE_REFERENCE,

    /**
     * A BDM Access Control rule grants access to no attribute at all
     */
    BDM_ACCESS_CONTROL_ZERO_ATTRIBUTE_GRANTED

}
