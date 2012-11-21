/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.io.PrintWriter;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptiste Mesta
 */
public class TechnicalLoggerPrintWriter extends PrintWriter {

    private final TechnicalLoggerService logger;

    private StringBuilder buffer;

    private final TechnicalLogSeverity level;

    private final Object mutex = new Object();

    public TechnicalLoggerPrintWriter(final TechnicalLoggerService logger, final TechnicalLogSeverity level) {
        super(System.out);
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void println(final Object x) {
        synchronized (mutex) {
            if (buffer != null) {
                logger.log(this.getClass(), level, buffer.toString());
                buffer = null;
            }
        }
        logger.log(this.getClass(), level, String.valueOf(x));
    }

    @Override
    public void print(final Object obj) {
        synchronized (mutex) {
            if (buffer == null) {
                buffer = new StringBuilder();
            }
            buffer.append(obj);
        }
    }

}
