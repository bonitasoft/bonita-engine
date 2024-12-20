/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.mdc;

/**
 * <p>These constants are used as logging keys for Mapped Diagnostic Context (MDC).</p>
 * <p>In addition, you may also use <code>REQUEST_*</code> constants in ch.qos.logback.classic.ClassicConstants:
 * <ul><li>req.remoteHost</li><li>req.userAgent</li><li>req.requestURI</li><li>req.queryString</li><li>req.requestURL</li><li>req.method</li><li>req.xForwardedFor</li></ul>
 * </p>
 */
public interface MDCConstants {

    /** The technical ID of the concerned process instance (a.k.a. case ID). */
    String PROCESS_INSTANCE_ID = "processInstanceId";
    /** The technical ID of the concerned root process instance (a.k.a. root case ID). */
    String ROOT_PROCESS_INSTANCE_ID = "rootProcessInstanceId";
    /** The technical ID of the process definition. */
    String PROCESS_DEFINITION_ID = "processDefinitionId";
    /** The technical ID of the executing flow node instance in the process. */
    String FLOW_NODE_INSTANCE_ID = "flowNodeInstanceId";
    /** The technical ID of the user who performed the current operation. */
    String USER_ID = "userId";
    /** The technical ID of the admin user who triggered an operation for another user. */
    String SUBSTITUTE_USER_ID = "substituteUserId";
    /** The ID of the JPA transaction. */
    String TRANSACTION_ID = "txUid";
    /** The ID of the HTTP request. */
    String REQUEST_ID = "req.requestId";
    /** The correlation ID identifying a larger operation than the HTTP request. */
    String CORRELATION_REQUEST_ID = "req.correlationId";

}
