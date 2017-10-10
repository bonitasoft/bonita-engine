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

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Map;

/**
 * A status represents a generic result of some action.
 * It has a severity level, a technical message (non internationalized),
 * a unique status code used for internationalization and
 * an optional context saving the implicated item (eg: a data or process name)
 */
public class Status implements Serializable {

    public enum Level {
        OK, ERROR, WARNING, INFO
    }

    private Level level;

    private String message;

    private StatusContext context;

    private StatusCode code;

    public static Status okStatus() {
        return new Status(Level.OK, StatusCode.OK, "", null);
    }

    public static Status errorStatus(StatusCode code, String message) {
        return errorStatus(code, message, null);
    }

    public static Status errorStatus(StatusCode code, String message, Map<String, Serializable> context) {
        return new Status(Level.ERROR, code, message, context);
    }

    public static Status warningStatus(StatusCode code, String message) {
        return warningStatus(code, message, null);
    }

    public static Status warningStatus(StatusCode code, String message, Map<String, Serializable> context) {
        return new Status(Level.WARNING, code, message, context);
    }

    public static Status infoStatus(StatusCode code, String message) {
        return infoStatus(code, message, null);
    }

    public static Status infoStatus(StatusCode code, String message, Map<String, Serializable> context) {
        return new Status(Level.INFO, code, message, context);
    }

    private Status(Level level, StatusCode code, String message, Map<String, Serializable> context) {
        this.level = level;
        this.code = code;
        this.message = requireNonNull(message);
        this.context = (context != null ? new StatusContext(context) : new StatusContext());
    }

    public StatusCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Level getLevel() {
        return level;
    }

    public StatusContext getContext() {
        return context;
    }

}
