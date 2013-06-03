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
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class GetSCommand implements TransactionContentWithResult<SCommand> {

    private final CommandService commandService;

    private final String name;

    private long id = -1;

    private SCommand command;

    public GetSCommand(final CommandService commandService, final String name) {
        this.commandService = commandService;
        this.name = name;
    }

    public GetSCommand(final CommandService commandService, final long id) {
        this.commandService = commandService;
        this.id = id;
        name = null;
    }

    @Override
    public void execute() throws SBonitaException {
        if (id != -1) {
            command = commandService.get(id);
        } else {
            command = commandService.get(name);
        }
    }

    @Override
    public SCommand getResult() {
        return command;
    }

}
