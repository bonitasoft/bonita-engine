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
package org.bonitasoft.engine.bpm.connector;

/**
 * Criterion to sort connectors
 * 
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public enum ConnectorCriterion {
    /**
     * By ascending identifier of connector definition
     */
    DEFINITION_ID_ASC,

    /**
     * By descending identifier of connector definition
     */
    DEFINITION_ID_DESC,

    /**
     * By ascending version of connector definition
     * 
     */
    DEFINITION_VERSION_ASC,

    /**
     * By descending version of connector definition
     */
    DEFINITION_VERSION_DESC,

    /**
     * By ascending identifier of connector implementation
     */
    
    IMPLEMENTATION_ID_ASC,
    
    /**
     * By descending identifier of connector implementation
     */
    IMPLEMENTATION_ID_DESC,
    
    /**
     * By ascending version of connector implementation
     */
    IMPLEMENTATION_VERSION_ASC,
    
    /**
     * By descending version of connector implementation
     */
    IMPLEMENTATIONN_VERSION_DESC,
    
    /**
     * By ascending class name of connector implementation
     */
    IMPLEMENTATIONN_CLASS_NAME_ACS,
    
    /**
     * By descending class name of connector implementation
     */
    IMPLEMENTATIONN_CLASS_NAME_DESC,
    
    /**
     * By descending identifier of connector implementation
     */
    DEFAULT
}
