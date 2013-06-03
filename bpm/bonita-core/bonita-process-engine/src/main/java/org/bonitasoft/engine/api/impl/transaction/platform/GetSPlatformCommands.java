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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.util.List;

import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandCriterion;

/**
 * @author Zhang Bole
 */
public class GetSPlatformCommands implements TransactionContentWithResult<List<SPlatformCommand>> {

    private final PlatformCommandService platformCommandService;

    private final int startIndex;

    private final int maxResults;

    private final CommandCriterion sort;

    private List<SPlatformCommand> platformCommands;

    public GetSPlatformCommands(final PlatformCommandService platformCommandService, final int startIndex, final int maxResults, final CommandCriterion sort) {
        super();
        this.platformCommandService = platformCommandService;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.sort = sort;
    }

    @Override
    public void execute() throws SBonitaException {
        SPlatformCommandCriterion sPlatformCommandCriterion = null;
        switch (sort) {
            case NAME_ASC:
                sPlatformCommandCriterion = SPlatformCommandCriterion.NAME_ASC;
                break;
            case NAME_DESC:
                sPlatformCommandCriterion = SPlatformCommandCriterion.NAME_DESC;
                break;
        }
        platformCommands = platformCommandService.getPlatformCommands(startIndex, maxResults, sPlatformCommandCriterion);
    }

    @Override
    public List<SPlatformCommand> getResult() {
        return platformCommands;
    }

}
