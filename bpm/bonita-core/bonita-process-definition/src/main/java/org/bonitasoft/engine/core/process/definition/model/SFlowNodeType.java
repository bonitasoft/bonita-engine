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
package org.bonitasoft.engine.core.process.definition.model;

/**
 * @author Zhang Bole
 * @author Baptitse Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public enum SFlowNodeType {

    AUTOMATIC_TASK("automatic task"),

    USER_TASK("user task"),

    MANUAL_TASK("manual task"),

    RECEIVE_TASK("receive task"),

    SEND_TASK("send task"),

    GATEWAY("gateway"),

    START_EVENT("start event"),

    INTERMEDIATE_CATCH_EVENT("intermediate catch event"),

    BOUNDARY_EVENT("boundary event"),

    INTERMEDIATE_THROW_EVENT("intermediate throw event"),

    END_EVENT("end event"),

    LOOP_ACTIVITY("loop activity"),

    MULTI_INSTANCE_ACTIVITY("multi-instance activity"),

    CALL_ACTIVITY("call activity"),

    SUB_PROCESS("sub process");

    private final String value;

    private SFlowNodeType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
