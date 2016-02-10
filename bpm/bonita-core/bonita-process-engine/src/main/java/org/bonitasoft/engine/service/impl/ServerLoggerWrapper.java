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
package org.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.api.Logger;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 *
 * Wrap the technical logger service to be available to client extensions
 *
 * @author Baptiste Mesta
 */
public class ServerLoggerWrapper implements Logger {

    private Class<?> clazz;
    private TechnicalLoggerService logger;

    public ServerLoggerWrapper(Class<?> clazz, TechnicalLoggerService logger) {
        this.clazz = clazz;
        this.logger = logger;
    }

    @Override
    public void trace(String message, Throwable t) {
        logger.log(clazz, TechnicalLogSeverity.TRACE, message, t);
    }

    @Override
    public void trace(String message) {
        logger.log(clazz, TechnicalLogSeverity.TRACE, message);
    }

    @Override
    public void debug(String message, Throwable t) {
        logger.log(clazz, TechnicalLogSeverity.DEBUG, message, t);
    }

    @Override
    public void debug(String message) {
        logger.log(clazz, TechnicalLogSeverity.DEBUG, message);
    }

    @Override
    public void info(String message, Throwable t) {
        logger.log(clazz, TechnicalLogSeverity.INFO, message, t);
    }

    @Override
    public void info(String message) {
        logger.log(clazz, TechnicalLogSeverity.INFO, message);
    }

    @Override
    public void warning(String message, Throwable t) {
        logger.log(clazz, TechnicalLogSeverity.WARNING, message, t);
    }

    @Override
    public void warning(String message) {
        logger.log(clazz, TechnicalLogSeverity.WARNING, message);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.log(clazz, TechnicalLogSeverity.ERROR, message, t);
    }

    @Override
    public void error(String message) {
        logger.log(clazz, TechnicalLogSeverity.ERROR, message);
    }
}
