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

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.CommandUpdater;
import org.bonitasoft.engine.command.CommandUpdater.CommandField;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilder;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class UpdateSCommand implements TransactionContent {

    private final CommandService commandService;

    private final SCommandUpdateBuilder commandUpdateBuilder;

    private final String name;

    private final CommandUpdater updateDescriptor;

    private long commandId = -1;

    public UpdateSCommand(final CommandService commandService, final SCommandUpdateBuilder commandUpdateBuilder, final String name,
            final CommandUpdater updateDescriptor) {
        this.commandService = commandService;
        this.commandUpdateBuilder = commandUpdateBuilder;
        this.name = name;
        this.updateDescriptor = updateDescriptor;
    }

    public UpdateSCommand(final CommandService commandService, final SCommandUpdateBuilder commandUpdateBuilder, final long commandId,
            final CommandUpdater updateDescriptor) {
        this.commandService = commandService;
        this.commandUpdateBuilder = commandUpdateBuilder;
        this.commandId = commandId;
        this.updateDescriptor = updateDescriptor;
        name = null;
    }

    @Override
    public void execute() throws SBonitaException {
        final EntityUpdateDescriptor changeDescriptor = getCommandUpdateDescriptor();
        SCommand sCommand = null;
        if (commandId != -1) {
            sCommand = commandService.get(commandId);
        } else {
            sCommand = commandService.get(name);
        }
        commandService.update(sCommand, changeDescriptor);
    }

    private EntityUpdateDescriptor getCommandUpdateDescriptor() {
        final Map<CommandField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<CommandField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    commandUpdateBuilder.updateName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    commandUpdateBuilder.updateDescription((String) field.getValue());
                    break;
            }
        }
        return commandUpdateBuilder.done();
    }

}
