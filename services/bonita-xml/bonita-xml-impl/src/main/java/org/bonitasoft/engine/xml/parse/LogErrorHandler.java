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
package org.bonitasoft.engine.xml.parse;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class LogErrorHandler implements ErrorHandler {

    private final TechnicalLoggerService logger;

    public LogErrorHandler(final TechnicalLoggerService logger) {
        this.logger = logger;
    }

    @Override
    public void error(final SAXParseException spe) {
        if (this.logger != null && this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.ERROR, spe.getMessage(), spe);
        }
    }

    @Override
    public void fatalError(final SAXParseException spe) {
        if (this.logger != null && this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.ERROR, spe.getMessage(), spe);
        }
    }

    @Override
    public void warning(final SAXParseException spe) {
        if (this.logger != null && this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.WARNING, spe.getMessage(), spe);
        }
    }

}
