/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
