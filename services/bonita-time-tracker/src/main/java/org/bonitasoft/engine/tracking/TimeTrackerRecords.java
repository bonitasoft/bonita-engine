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
package org.bonitasoft.engine.tracking;

public enum TimeTrackerRecords {

    /**
     * this key is used to track the connector execution (execute method only, not in/out parameters processing) including the pool submission (that may have
     * additional impact if the pool is full).
     */
    EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT,

    /**
     * this key is used to track the connector execution (execute method only, not in/out parameters processing), without potential pool submission impact
     */
    EXECUTE_CONNECTOR_CALLABLE,

    /**
     * this key is used to track connector output parameters processing only (not pooling, not input, not execute)
     */
    EXECUTE_CONNECTOR_OUTPUT_OPERATIONS,

    /**
     * this key is used to track connector input parameters processing only (not pooling, not execute, not output)
     */
    EXECUTE_CONNECTOR_INPUT_EXPRESSIONS,

    /**
     * this key is used to track only the call to disconnect on a connector
     */
    EXECUTE_CONNECTOR_DISCONNECT,

    /**
     * this key is used to track the whole connector execution including pooling, input, execute, output and disconnect
     */
    EXECUTE_CONNECTOR_WORK,

    /**
     * this key is used to track the whole expression evaluation including its context. See ExpressionResolver.
     */
    EVALUATE_EXPRESSION_INCLUDING_CONTEXT,

    /**
     * this key is used to track the expression evaluation "only", assuming the context is already evaluated if necessary. See ExpressionService.
     */
    EVALUATE_EXPRESSION,

    /**
     * this key is used to track the expression evaluation "only", assuming the context is already evaluated if necessary. Evaluates a set of expression in one
     * measure. See ExpressionService.
     */
    EVALUATE_EXPRESSIONS,

}
