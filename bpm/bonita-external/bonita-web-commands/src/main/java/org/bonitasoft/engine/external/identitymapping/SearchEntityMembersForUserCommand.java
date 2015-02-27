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
package org.bonitasoft.engine.external.identitymapping;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Parameter keys: USER_ID_KEY: the ID of the user to search for, DISCRIMINATOR_ID_KEY : the discriminator to isolate the different functional notions,
 * SEARCH_OPTIONS_KEY: the Search options to filter & sort the results.
 * 
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchEntityMembersForUserCommand extends EntityMemberCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        final String kind = getStringMandadoryParameter(parameters, DISCRIMINATOR_ID_KEY);
        final Long userId = getUserIdParameter(parameters);
        final String externalId = getStringMandadoryParameter(parameters, EXTERNAL_ID_KEY);
        final SearchOptions searchOptions = getMandatoryParameter(parameters, SEARCH_OPTIONS_KEY, "Parameters map must contain an entry " + SEARCH_OPTIONS_KEY
                + " with a SearchOptions value");
        try {
            return searchEntityMembersInvolvingUser(kind, userId, externalId, searchOptions);
        } catch (SCommandExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'SearchEntityMembersCommand'", e);
        }
    }

}
