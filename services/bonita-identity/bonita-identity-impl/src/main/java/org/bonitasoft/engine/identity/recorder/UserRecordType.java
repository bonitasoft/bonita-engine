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
package org.bonitasoft.engine.identity.recorder;

/**
 * List all possible change of elements of the identity service
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public enum UserRecordType {

    DELETE_USER, ADD_USER, UPDATE_USER_PASSWORD, ADD_PROFILE_METADATA_DEFINITION, ADD_PROFILE_METADATA_VALUE, DELETE_PROFILE_METADATA_DEFINITION, DELETE_PROFILE_METADATA_VALUE, DELETE_ROLE, ADD_GROUP, ADD_ROLE, DELETE_GROUP, DELETE_USER_MEMBERSHIP, setUsersMemberships, UPDATE_USER, UPDATE_ROLE, UPDATE_GROUP, ADD_USER_MEMBERSHIP, UPDTAE_PROFILE_METADATA_DEFINITION, UPDATE_PROFILE_METADATA_VALUE, UPDATE_USER_MEMBERSHIP;

}
