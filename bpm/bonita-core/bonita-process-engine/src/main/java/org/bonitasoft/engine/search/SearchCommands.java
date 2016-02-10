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
package org.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.descriptor.SearchCommandDescriptor;

/**
 * @author Yanyan Liu
 */
public class SearchCommands extends AbstractCommandSearchEntity {

    private final CommandService commandService;

    public SearchCommands(final CommandService commandService, final SearchCommandDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.commandService = commandService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return commandService.getNumberOfCommands(searchOptions);
    }

    @Override
    public List<SCommand> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return commandService.searchCommands(searchOptions);
    }

}
