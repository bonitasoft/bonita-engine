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
package org.bonitasoft.engine.identity.xml;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class OrganizationMappingConstants {

    public static final String IDENTITY_ORGANIZATION = "Organization";// "identity:Organization";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String USERS = "users";

    public static final String USER = "user";

    public static final String ROLES = "roles";

    public static final String ROLE = "role";

    public static final String GROUPS = "groups";

    public static final String GROUP = "group";

    public static final String MEMBERSHIPS = "memberships";

    public static final String MEMBERSHIP = "membership";

    public static final String VALUE = "value";

    // user membership

    public static final String ASSIGNED_DATE = "assignedDate";

    public static final String ROLE_NAME = "roleName";

    public static final String GROUP_NAME = "groupName";

    public static final String GROUP_PARENT_PATH = "groupParentPath";

    public static final String ASSIGNED_BY = "assignedBy";

    // user
    public static final String PASSWORD_ENCRYPTED = "encrypted";

    public static final String PASSWORD = "password";

    public static final String FIRST_NAME = "firstName";

    public static final String LAST_NAME = "lastName";

    public static final String USER_NAME = "userName";

    public static final String ICON_NAME = "iconName";

    public static final String ICON_PATH = "iconPath";

    public static final String TITLE = "title";

    public static final String JOB_TITLE = "jobTitle";

    public static final String ENABLED = "enabled";

    // TODO delete manager
    public static final String MANAGER = "manager";

    public static final String PERSONAL_DATA = "personalData";

    public static final String PROFESSIONAL_DATA = "professionalData";

    public static final String METADATA = "metaData";

    // role

    static final String DISPLAY_NAME = "displayName";

    // group

    public static final String PARENT_PATH = "parentPath";

    // contact info
    static final String WEBSITE = "website";

    static final String COUNTRY = "country";

    static final String STATE = "state";

    static final String CITY = "city";

    static final String ZIP_CODE = "zipCode";

    static final String ADDRESS = "address";

    static final String ROOM = "room";

    static final String BUILDING = "building";

    static final String FAX_NUMBER = "faxNumber";

    static final String MOBILE_NUMBER = "mobileNumber";

    static final String PHONE_NUMBER = "phoneNumber";

    static final String EMAIL = "email";

    static final String CUSTOM_USER_INFO_DEFINITION = "customUserInfoDefinition";

    static final String CUSTOM_USER_INFO_DEFINITIONS = "customUserInfoDefinitions";
    
    static final String CUSTOM_USER_INFO_VALUE = "customUserInfoValue";
    
    static final String CUSTOM_USER_INFO_VALUES = "customUserInfoValues";

}
