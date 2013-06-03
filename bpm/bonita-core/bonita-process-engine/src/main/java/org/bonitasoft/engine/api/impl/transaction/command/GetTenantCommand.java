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
package org.bonitasoft.engine.api.impl.transaction.command;

import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class GetTenantCommand implements TransactionContentWithResult<TenantCommand> {

    private final CommandService commandService;

    private final String name;

    private long id = -1;

    private TenantCommand tenantCommand;

    public GetTenantCommand(final CommandService commandService, final String name) {
        super();
        this.commandService = commandService;
        this.name = name;
    }

    public GetTenantCommand(final CommandService commandService, final long id) {
        this.commandService = commandService;
        this.id = id;
        name = null;
    }

    @Override
    public void execute() throws SBonitaException {
        try {
            SCommand sCommand;
            if (id != -1) {
                sCommand = commandService.get(id);
            } else {
                sCommand = commandService.get(name);
            }

            final String tenantCommandClassName = sCommand.getImplementation();
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            tenantCommand = (TenantCommand) contextClassLoader.loadClass(tenantCommandClassName).newInstance();
        } catch (final ClassNotFoundException cnfe) {
            throw new SCommandParameterizationException(cnfe);
        } catch (final InstantiationException ie) {
            throw new SCommandParameterizationException(ie);
        } catch (final IllegalAccessException iae) {
            throw new SCommandParameterizationException(iae);
        }
    }

    @Override
    public TenantCommand getResult() {
        return tenantCommand;
    }

}
