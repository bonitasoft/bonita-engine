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

    /*****************************
     * ACCESS CONTROL STATUS CODE
     *****************************/

    /**
     * Everything is fine
     */
    OK,

    /**
     * Uploaded file is a null byte array. Not supported.
     */
    BDM_ACCESS_CONTROL_FILE_EMPTY,

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
    BDM_ACCESS_CONTROL_ZERO_ATTRIBUTE_GRANTED,

    /**
     * A Business Object has no rule defined at all
     */
    BDM_ACCESS_CONTROL_BUSINESS_OBJECT_WITHOUT_RULE,

    /**
     * A Business Object has empty rules
     */
    BDM_ACCESS_CONTROL_BUSINESS_OBJECT_WITH_EMPTY_RULES,

    /**
     * A BDM Access Control rule grants access to no profile at all
     */
    BDM_ACCESS_CONTROL_ZERO_PROFILE_GRANTED,

    /**
     * There is no BDM installed
     */
    BDM_NOT_INSTALLED,

    /**
     * An object in the BDM is referenced as if it was in a composition relationship, when it is not.
     */
    BDM_ACCESS_CONTROL_NON_COMPOSED_OBJECT_REFERENCED_IN_COMPOSITION,

    /**
     * An object used in composition in the BDM has rules on the first level of the Access Control file.
     */
    BDM_ACCESS_CONTROL_FIRST_LEVEL_RULES_ON_COMPOSED_OBJECT,

    /**
     * The naming of rules and objects in the access control file is inconsistent
     */
    BDM_ACCESS_CONTROL_INCONSISTENT_NAME_IN_FILE,

    /**********************************
     * Business Data Model Status Code
     **********************************/

    DUPLICATE_CONSTRAINT_OR_INDEX_NAME,

    UNIQUE_CONSTRAINT_WITHOUT_NAME,

    INVALID_SQL_IDENTIFIER_NAME,

    UNIQUE_CONSTRAINT_WITHOUT_FIELD,

    FIELD_WITHOUT_NAME,

    QUERY_WITHOUT_NAME,

    INVALID_JAVA_IDENTIFIER_NAME,

    QUERY_NAME_LENGTH_TO_HIGH,

    QUERY_WITHOUT_CONTENT,

    QUERY_WITHOUT_RETURN_TYPE,

    QUERY_PARAMETER_WITHOUT_NAME,

    FORBIDDEN_QUERY_PARAMETER_NAME,

    QUERY_PARAMETER_WITHOUT_CLASS_NAME,

    INDEX_WITHOUT_NAME,

    INDEX_WITHOUT_FIELD,

    INVALID_FIELD_IDENTIFIER,

    BUSINESS_OBJECT_WITHOUT_NAME,

    RESERVED_PACKAGE_NAME,

    INVALID_CHARACTER_IN_BUSINESS_OBJECT_NAME,

    BUSINESS_OBJECT_WITHOUT_FIELD,

    DUPLICATE_QUERY_NAME,

    DUPLICATE_CONSTRAINT_NAME,

    UNKNOWN_FIELD_IN_CONSTRAINT,

    EMPTY_BDM,

    SEVERAL_COMPOSITION_REFERENCE_FOR_A_BUSINESS_OBJECT,

    CIRCULAR_COMPOSITION_REFERENCE,

    BUSINESS_OBJECT_USED_IN_COMPOSITION_AND_AGGREGATION,

    MULTIPLE_AGGREGATION_RELATION_TO_ITSELF

}
