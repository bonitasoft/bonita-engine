/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class TechnicalLoggerSLF4JImpl implements TechnicalLoggerService {

    private final Map<Class<?>, Boolean> classIsTraceLoggable = new HashMap<Class<?>, Boolean>();

    private final Map<Class<?>, Boolean> classIsDebugLoggable = new HashMap<Class<?>, Boolean>();

    private final Map<Class<?>, Boolean> classIsInfoLoggable = new HashMap<Class<?>, Boolean>();

    private final Map<Class<?>, Boolean> classIsWarnLoggable = new HashMap<Class<?>, Boolean>();

    private final Map<Class<?>, Boolean> classIsErrorLoggable = new HashMap<Class<?>, Boolean>();

    private final Map<Class<?>, Logger> classLoggers = new HashMap<Class<?>, Logger>();

    public TechnicalLoggerSLF4JImpl() {
        super();
    }

    @Override
    public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final Throwable t) {
        log(callerClass, severity, t.getMessage(), t);
    }

    @Override
    public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message) {
        final Logger logger = getLogger(callerClass);
        switch (severity) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARNING:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
            default:
                logger.error("Trying to log using an unknow severity, using ERROR instead:" + severity.name());
                logger.error(message);
                break;
        }
    }

    @Override
    public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message, final Throwable t) {
        switch (severity) {
            case TRACE:
                getLogger(callerClass).trace(message, t);
                break;
            case DEBUG:
                getLogger(callerClass).debug(message, t);
                break;
            case INFO:
                getLogger(callerClass).info(message, t);
                break;
            case WARNING:
                getLogger(callerClass).warn(message, t);
                break;
            case ERROR:
                getLogger(callerClass).error(message, t);
                break;
            default:
                getLogger(callerClass).error("Trying to log using an unknow severity, using ERROR instead:" + severity.name());
                getLogger(callerClass).error(message);
                break;
        }
    }

    @Override
    public boolean isLoggable(final Class<?> callerClass, final TechnicalLogSeverity severity) {
        switch (severity) {
            case TRACE:
                return isEnabledForLevel(callerClass, classIsTraceLoggable, severity);
            case DEBUG:
                return isEnabledForLevel(callerClass, classIsDebugLoggable, severity);
            case INFO:
                return isEnabledForLevel(callerClass, classIsInfoLoggable, severity);
            case WARNING:
                return isEnabledForLevel(callerClass, classIsWarnLoggable, severity);
            case ERROR:
                return isEnabledForLevel(callerClass, classIsErrorLoggable, severity);
            default:
                getLogger(callerClass).error("Trying to log using an unknow severity, using ERROR instead:" + severity.name());
                return isEnabledForLevel(callerClass, classIsErrorLoggable, TechnicalLogSeverity.ERROR);
        }
    }

    private boolean isEnabledForLevel(final Class<?> callerClass, final Map<Class<?>, Boolean> loggableMap, final TechnicalLogSeverity severity) {
        Boolean isLevelEnabled = loggableMap.get(callerClass);
        if (isLevelEnabled == null) {
            switch (severity) {
                case DEBUG:
                    isLevelEnabled = getLogger(callerClass).isDebugEnabled();
                    break;
                case ERROR:
                    isLevelEnabled = getLogger(callerClass).isErrorEnabled();
                    break;
                case INFO:
                    isLevelEnabled = getLogger(callerClass).isInfoEnabled();
                    break;
                case TRACE:
                    isLevelEnabled = getLogger(callerClass).isTraceEnabled();
                    break;
                case WARNING:
                    isLevelEnabled = getLogger(callerClass).isWarnEnabled();
                    break;
                default:
                    isLevelEnabled = false;
                    break;
            }
            loggableMap.put(callerClass, isLevelEnabled);
        }
        return isLevelEnabled;
    }

    /**
     * Get the logger from the logger "cache", or retrieves it if not found (and then put it in "cache")
     * 
     * @param clazz
     *            the class of the logger to retrieve
     * @return the logger for the given class
     * @since 6.0
     */
    private Logger getLogger(final Class<?> clazz) {
        Logger logger = classLoggers.get(clazz);
        if (logger == null) {
            logger = LoggerFactory.getLogger(clazz);
            classLoggers.put(clazz, logger);
        }
        return logger;
    }

}
