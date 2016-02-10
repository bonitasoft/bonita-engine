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
package org.bonitasoft.engine.api.impl.transaction.platform;

import org.bonitasoft.engine.command.PlatformCommand;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

/**
 * @author Zhang Bole
 */
public class GetPlatformCommand implements TransactionContentWithResult<PlatformCommand> {

    private final String platformCommandClassName;

    private PlatformCommand command;

    public GetPlatformCommand(final String platformCommandClassName) {
        super();
        this.platformCommandClassName = platformCommandClassName;
    }

    @Override
    public void execute() throws SBonitaException {
        try {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            command = (PlatformCommand) contextClassLoader.loadClass(platformCommandClassName).newInstance();
        } catch (final ClassNotFoundException cnfe) {
            throw new SCommandParameterizationException(cnfe);
        } catch (final InstantiationException ie) {
            throw new SCommandParameterizationException(ie);
        } catch (final IllegalAccessException iae) {
            throw new SCommandParameterizationException(iae);
        }
    }

    @Override
    public PlatformCommand getResult() {
        return command;
    }

}
