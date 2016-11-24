/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;

/**
 * @author Baptiste Mesta
 */
public abstract class PrintTestsStatusRule extends TestWatcher {

    private final Logger logger;

    public PrintTestsStatusRule(Logger logger) {

        this.logger = logger;
    }

    @Override
    public void starting(final Description d) {
        logger.warn("Starting test: " + d.getClassName() + "." + d.getMethodName());
    }

    @Override
    public void failed(final Throwable e, final Description d) {
        logger.warn("Failed test: " + d.getClassName() + "." + d.getMethodName(), e);
        try {
            clean();
        } catch (final Exception be) {
            logger.error("unable to clean db", be);
        } finally {
            logger.warn("-----------------------------------------------------------------------------------------------");
        }
    }

    @Override
    public void succeeded(final Description d) {
        try {
            try {
                clean();
            } catch (final Exception e) {
                throw new BonitaRuntimeException(e);
            }
            logger.warn("Succeeded test: " + d.getClassName() + "." + d.getMethodName());
        } finally {
            logger.warn("-----------------------------------------------------------------------------------------------");
        }
    }

    public abstract void clean() throws Exception;
}
