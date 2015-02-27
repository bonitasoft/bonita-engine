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
package org.bonitasoft.engine.commons;

/**
 * This class is for Add call to techinalLoggerService.log with severity TRACE in all Service Implementation public method
 * 
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public class LogUtil {

    /**
     * Get the log message on enter of the method
     * 
     * @param classes
     *            class name
     * @param methodName
     *            method name
     * @return log message with String type
     */
    public static String getLogBeforeMethod(final Object classes, final String methodName) {
        final String serviceName = classes.toString();
        return "Executing method " + methodName + " on Service " + serviceName;
    }

    /**
     * Get the log message before quitting the method
     * 
     * @param classes
     *            class name
     * @param methodName
     *            method name
     * @return log message with String type
     */
    public static String getLogAfterMethod(final Object classes, final String methodName) {
        final String serviceName = classes.toString();
        return "Quitting method " + methodName + " on Service " + serviceName;
    }

    /**
     * Get the log message in each catch block
     * 
     * @param classes
     *            class name
     * @param methodName
     *            method name
     * @param e
     *            Exception
     * @return log message with String type
     */
    public static String getLogOnExceptionMethod(final Object classes, final String methodName, final Exception e) {
        final String serviceName = classes.toString();
        return "Quitting method " + methodName + " on Service " + serviceName + " with exception " + e.getMessage();
    }

    /**
     * Get the log message in each catch block
     * 
     * @param classes
     * @param methodName
     * @param exceptionMessage
     * @return log message with String type
     */
    public static String getLogOnExceptionMethod(final Object classes, final String methodName, final String exceptionMessage) {
        final String serviceName = classes.toString();
        return "Quitting method " + methodName + " on Service " + serviceName + " with exception" + exceptionMessage;
    }

}
