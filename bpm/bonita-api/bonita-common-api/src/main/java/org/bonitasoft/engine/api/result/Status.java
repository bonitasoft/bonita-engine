/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.api.result;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Optional;

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

    private String context;

    private StatusCode code;

    public static Status okStatus() {
        return new Status(Level.OK, StatusCode.OK, "", null);
    }

    public static Status errorStatus(StatusCode code, String message) {
        return new Status(Level.ERROR, code, message, null);
    }

    public static Status errorStatus(StatusCode code, String message, String context) {
        return new Status(Level.ERROR, code, message, context);
    }

    public static Status warningStatus(StatusCode code, String message) {
        return new Status(Level.WARNING, code, message, null);
    }

    public static Status warningStatus(StatusCode code, String message, String context) {
        return new Status(Level.WARNING, code, message, context);
    }

    public static Status infoStatus(StatusCode code, String message) {
        return new Status(Level.INFO, code, message, null);
    }

    public static Status infoStatus(StatusCode code, String message, String context) {
        return new Status(Level.INFO, code, message, context);
    }

    private Status(Level level, StatusCode code, String message, String context) {
        this.level = level;
        this.code = code;
        this.message = requireNonNull(message);
        this.context = context;
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

    public Optional<String> getContext() {
        return Optional.ofNullable(context);
    }

}
