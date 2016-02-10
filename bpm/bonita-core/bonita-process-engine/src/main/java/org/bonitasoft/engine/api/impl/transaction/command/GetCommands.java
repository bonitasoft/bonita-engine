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

import java.util.List;

import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

/**
 * @author Hongwen Zang
 * @since 6.0
 */
public class GetCommands implements TransactionContentWithResult<List<SCommand>> {

    private final CommandService commandService;

    private final int startIndex;

    private final int maxResults;

    private final CommandCriterion sort;

    private List<SCommand> commands;

    public GetCommands(final CommandService commandService, final int startIndex, final int maxResults, final CommandCriterion sort) {
        super();
        this.commandService = commandService;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.sort = sort;
    }

    @Override
    public void execute() throws SBonitaException {
        SCommandCriterion sCommandCriterion = null;
        if (CommandCriterion.NAME_ASC.equals(sort)) {
            sCommandCriterion = SCommandCriterion.NAME_ASC;
        } else {
            sCommandCriterion = SCommandCriterion.NAME_DESC;
        }
        commands = commandService.getUserCommands(startIndex, maxResults, sCommandCriterion);
    }

    @Override
    public List<SCommand> getResult() {
        return commands;
    }
}
