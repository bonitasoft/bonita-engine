/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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

/**
 * @author Baptiste Mesta
 */
public interface TechnicalLoggerService {

    /**
     * Provides a <code>TechnicalLogger</code> backed to this <code>TechnicalLoggerService</code>.
     * @since 7.8.0
     */
    TechnicalLogger asLogger(Class<?> clazz);

    void log(Class<?> callerClass, TechnicalLogSeverity severity, String message);

    /**
     * Generates parametrized logs like <a href="https://www.slf4j.org/faq.html#logging_performance">slf4j</a> does.
     * <br/>
     * Usage example: <br/>
     * <code>
     * log(getClass(), INFO, "Ready to update process {} with {}", processId, complexObjectWithSlowToStringMethod);
     * </code>
     * 
     * @since 7.8.0
     */
    void log(Class<?> callerClass, TechnicalLogSeverity severity, String message, Object... arguments);

    void log(Class<?> callerClass, TechnicalLogSeverity severity, String message, Throwable t);

    void log(Class<?> callerClass, TechnicalLogSeverity severity, Throwable t);

    boolean isLoggable(Class<?> callerClass, TechnicalLogSeverity severity);

}
