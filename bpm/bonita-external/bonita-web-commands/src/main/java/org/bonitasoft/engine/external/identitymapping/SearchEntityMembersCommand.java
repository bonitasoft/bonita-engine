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
package org.bonitasoft.engine.external.identitymapping;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityMemberDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityMemberGroupDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityMemberRoleAndGroupDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityMemberRoleDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityMemberUserDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Parameter keys: DISCRIMINATOR_ID_KEY: the discriminator to isolate the different functional notions, SEARCH_OPTIONS_KEY: the Search options to filter & sort
 * the results, MEMBER_TYPE_KEY: Member Type to search for (USER, GROUP, ROLE, or MEMBERSHIP).
 * 
 * @author Emmanuel Duchastenier
 */
public class SearchEntityMembersCommand extends EntityMemberCommand {

    private static final String MEMBER_TYPE_KEY = "MEMBER_TYPE_KEY";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;

        final String kind = getStringMandadoryParameter(parameters, DISCRIMINATOR_ID_KEY);
        final SearchOptions searchOptions = getMandatoryParameter(parameters, SEARCH_OPTIONS_KEY, "Parameters map must contain an entry " + SEARCH_OPTIONS_KEY
                + " with a SearchOptions value");
        final MemberType memberType = getMandatoryParameter(parameters, MEMBER_TYPE_KEY, "Parameters map must contain an entry " + MEMBER_TYPE_KEY
                + " with a MemberType value (USER, GROUP, ROLE, MEMBERSHIP)");

        final String querySuffix = getQuerySuffix(memberType);
        final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
        final SExternalIdentityMappingBuilders builders = serviceAccessor.getExternalIdentityMappingBuilders();
        final SearchEntityMemberDescriptor searchDescriptor = getSearchDescriptor(builders, identityModelBuilder, memberType);

        try {
            return searchEntityMembers(searchDescriptor, kind, searchOptions, querySuffix);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'SearchEntityMembersCommand'", e);
        }
    }

    private SearchEntityMemberDescriptor getSearchDescriptor(final SExternalIdentityMappingBuilders builders, final IdentityModelBuilder identityModelBuilder,
            final MemberType memberType) {
        SearchEntityMemberDescriptor searchDescriptor = null;
        switch (memberType) {
            case USER:
                searchDescriptor = new SearchEntityMemberUserDescriptor(builders, identityModelBuilder);
                break;

            case GROUP:
                searchDescriptor = new SearchEntityMemberGroupDescriptor(builders, identityModelBuilder);
                break;

            case ROLE:
                searchDescriptor = new SearchEntityMemberRoleDescriptor(builders, identityModelBuilder);
                break;

            case MEMBERSHIP:
                searchDescriptor = new SearchEntityMemberRoleAndGroupDescriptor(builders, identityModelBuilder);
                break;

        }
        return searchDescriptor;
    }
}
