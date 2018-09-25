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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class TechnicalLoggerSLF4JImpl implements TechnicalLoggerService {

    private String hostname = null;

    private final long tenantId;

    public TechnicalLoggerSLF4JImpl() {
        this(-1);
    }

    public TechnicalLoggerSLF4JImpl(final long tenantId) {
        super();

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            // Nothing to do
        }

        this.tenantId = tenantId;
    }

    @Override
    public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final Throwable t) {
        log(callerClass, severity, t.getMessage(), t);
    }

    @Override
    public TechnicalLogger asLogger(Class<?> clazz) {
        return new TechnicalLogger(clazz, this);
    }

    @Override
    public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message) {
        if (!isLoggable(callerClass, severity)) { // prevent computation of the loggedMessage if not needed
            return;
        }
        final Logger logger = getLogger(callerClass);
        final String loggedMessage = getContextMessage() + message;
        switch (severity) {
            case TRACE:
                logger.trace(loggedMessage);
                break;
            case DEBUG:
                logger.debug(loggedMessage);
                break;
            case INFO:
                logger.info(loggedMessage);
                break;
            case WARNING:
                logger.warn(loggedMessage);
                break;
            case ERROR:
                logger.error(loggedMessage);
                break;
            default:
                logger.error("Trying to log using an unknown severity, using ERROR instead: {}", severity);
                logger.error(loggedMessage);
                break;
        }
    }

    public void log(Class<?> callerClass, TechnicalLogSeverity severity, String message, Object... arguments) {
        if (!isLoggable(callerClass, severity)) { // prevent computation of the loggedMessage if not needed
            return;
        }
        final Logger logger = getLogger(callerClass);
        final String loggedMessage = getContextMessage() + message;

        switch (severity) {
            case TRACE:
                logger.trace(loggedMessage, arguments);
                break;
            case DEBUG:
                logger.debug(loggedMessage, arguments);
                break;
            case INFO:
                logger.info(loggedMessage, arguments);
                break;
            case WARNING:
                logger.warn(loggedMessage, arguments);
                break;
            case ERROR:
                logger.error(loggedMessage, arguments);
                break;
            default:
                logger.error("Trying to log using an unknown severity, using ERROR instead: {}", severity);
                logger.error(loggedMessage, arguments);
                break;
        }
    }

    @Override
    public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message,
            final Throwable t) {
        if (!isLoggable(callerClass, severity)) { // prevent computation of the loggedMessage if not needed
            return;
        }
        final Logger logger = getLogger(callerClass);
        final String loggedMessage = getContextMessage() + message;
        switch (severity) {
            case TRACE:
                logger.trace(loggedMessage, t);
                break;
            case DEBUG:
                logger.debug(loggedMessage, t);
                break;
            case INFO:
                logger.info(loggedMessage, t);
                break;
            case WARNING:
                logger.warn(loggedMessage, t);
                break;
            case ERROR:
                logger.error(loggedMessage, t);
                break;
            default:
                logger.error("Trying to log using an unknown severity, using ERROR instead: {}", severity);
                logger.error(loggedMessage);
                break;
        }
    }

    @Override
    public boolean isLoggable(final Class<?> callerClass, final TechnicalLogSeverity severity) {
        final Logger logger = getLogger(callerClass);
        switch (severity) {
            case TRACE:
                return logger.isTraceEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case WARNING:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            default:
                logger.error("Trying to log using an unknown severity, using ERROR instead: {}", severity);
                return logger.isErrorEnabled();
        }
    }

    private Logger getLogger(final Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    // TODO we should use slf4j MDC instead, see https://www.slf4j.org/manual.html#mdc
    // not supported by JUL, to be checked for JBoss-logging
    private String getContextMessage() {
        return getThreadIdMessage() + getHostNameMessage() + getTenantIdMessage();
    }

    private String getThreadIdMessage() {
        final long threadId = Thread.currentThread().getId();
        return "THREAD_ID=" + threadId + " | ";
    }

    private String getHostNameMessage() {
        return hostname != null && !hostname.isEmpty() ? "HOSTNAME=" + hostname + " | " : "";
    }

    private String getTenantIdMessage() {
        return tenantId != -1 ? "TENANT_ID=" + tenantId + " | " : "";
    }

}
