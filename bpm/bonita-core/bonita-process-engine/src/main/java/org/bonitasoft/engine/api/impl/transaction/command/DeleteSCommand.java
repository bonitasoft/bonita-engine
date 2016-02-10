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
package org.bonitasoft.engine.api.impl.transaction.command;

import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class DeleteSCommand implements TransactionContent {

    private final String name;

    private final CommandService commandService;

    private long commandId;

    public DeleteSCommand(final CommandService commandService, final String name) {
        this.commandService = commandService;
        this.name = name;
    }

    public DeleteSCommand(final CommandService commandService, final long commandId) {
        this.commandService = commandService;
        this.commandId = commandId;
        name = null;
    }

    @Override
    public void execute() throws SBonitaException {
        if (name == null) {
            commandService.delete(commandId);
        } else {
            commandService.delete(name);
        }
    }

}
