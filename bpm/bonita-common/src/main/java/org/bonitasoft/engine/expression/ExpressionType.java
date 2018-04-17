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
package org.bonitasoft.engine.expression;

/**
 * Default expression type supported in the engine
 * Other type of expression can be added by adding a new expression executor strategy having an unique kind
 *
 * @author Zhao na
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public enum ExpressionType {

    TYPE_I18N,

    TYPE_CONSTANT,

    TYPE_INPUT,

    TYPE_READ_ONLY_SCRIPT,

    TYPE_READ_WRITE_SCRIPT,

    TYPE_VARIABLE,

    TYPE_TRANSIENT_VARIABLE,

    TYPE_PATTERN,

    TYPE_PARAMETER,

    TYPE_DOCUMENT,

    /**
     * expressions that return a list of document
     *
     * @since 6.4.0
     */
    TYPE_DOCUMENT_LIST,

    TYPE_ENGINE_CONSTANT,

    TYPE_LIST,

    TYPE_CONDITION,

    TYPE_XPATH_READ,

    TYPE_JAVA_METHOD_CALL,

    TYPE_BUSINESS_DATA,

    TYPE_BUSINESS_DATA_REFERENCE,

    /**
     * Expression of type Business object DAO, that instantiates a business object Server DAO class, used to execute server queries.
     */
    TYPE_BUSINESS_OBJECT_DAO,

    TYPE_QUERY_BUSINESS_DATA,

    TYPE_CONTRACT_INPUT

}
