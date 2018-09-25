/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
package org.bonitasoft.engine.log.technical;

import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.*;

/**
 * A logger backed to a Technical Logger Service which provides provides methods like <a href="https://www.slf4j.org/apidocs/org/slf4j/Logger.html">slf4j</a>.
 */
public class TechnicalLogger {

    private Class<?> clazz;
    private TechnicalLoggerService technicalLoggerService;

    public TechnicalLogger(Class<?> clazz, TechnicalLoggerService technicalLoggerService) {
        this.clazz = clazz;
        this.technicalLoggerService = technicalLoggerService;
    }

    // =================================================================================================================
    // TRACE methods
    // =================================================================================================================

    public boolean isTraceEnabled() {
        return technicalLoggerService.isLoggable(clazz, TRACE);
    }

    public void trace(String message) {
        technicalLoggerService.log(clazz, TRACE, message);
    }

    public void trace(String message, Object... arguments) {
        technicalLoggerService.log(clazz, TRACE, message, arguments);
    }

    public void trace(String message, Throwable t) {
        technicalLoggerService.log(clazz, TRACE, message, t);
    }

    // =================================================================================================================
    // DEBUG methods
    // =================================================================================================================

    public boolean isDebugEnabled() {
        return technicalLoggerService.isLoggable(clazz, DEBUG);
    }

    public void debug(String message) {
        technicalLoggerService.log(clazz, DEBUG, message);
    }

    public void debug(String message, Object... arguments) {
        technicalLoggerService.log(clazz, DEBUG, message, arguments);
    }

    public void debug(String message, Throwable t) {
        technicalLoggerService.log(clazz, DEBUG, message, t);
    }

    // =================================================================================================================
    // INFO methods
    // =================================================================================================================

    public boolean isInfoEnabled() {
        return technicalLoggerService.isLoggable(clazz, INFO);
    }

    public void info(String message) {
        technicalLoggerService.log(clazz, INFO, message);
    }

    public void info(String message, Object... arguments) {
        technicalLoggerService.log(clazz, INFO, message, arguments);
    }

    public void info(String message, Throwable t) {
        technicalLoggerService.log(clazz, INFO, message, t);
    }

    // =================================================================================================================
    // WARN methods
    // =================================================================================================================

    public boolean isWarnEnabled() {
        return technicalLoggerService.isLoggable(clazz, WARNING);
    }

    public void warn(String message) {
        technicalLoggerService.log(clazz, WARNING, message);
    }

    public void warning(String message, Throwable t) {
        warn(message, t);
    }

    public void warning(String message) {
        warn(message);
    }

    public void warn(String message, Object... arguments) {
        technicalLoggerService.log(clazz, WARNING, message, arguments);
    }

    public void warn(String message, Throwable t) {
        technicalLoggerService.log(clazz, WARNING, message, t);
    }

    // =================================================================================================================
    // ERROR methods
    // =================================================================================================================

    public boolean isErrorEnabled() {
        return technicalLoggerService.isLoggable(clazz, ERROR);
    }

    public void error(String message) {
        technicalLoggerService.log(clazz, ERROR, message);
    }

    public void error(String message, Object... arguments) {
        technicalLoggerService.log(clazz, ERROR, message, arguments);
    }

    public void error(String message, Throwable t) {
        technicalLoggerService.log(clazz, ERROR, message, t);
    }

}
