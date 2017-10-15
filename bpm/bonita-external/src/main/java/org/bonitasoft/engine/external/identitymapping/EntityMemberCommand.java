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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.entitymember.EntityMember;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityMemberDescriptor;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class EntityMemberCommand extends ExternalIdentityMappingCommand {

    protected SearchResult<EntityMember> searchEntityMembersInvolvingUser(final String kind, final long userId, final String externalId,
            final SearchOptions searchOptions) throws SBonitaException {
        EntityMemberSearchEntityForUser transactionContent = new EntityMemberSearchEntityForUser(serviceAccessor.getSearchEntitiesDescriptor()
                .getSearchEntityMemberUserDescriptor(), kind, userId, externalId, searchOptions);
        transactionContent.execute();
        return transactionContent.getResult();
    }

    protected SearchResult<EntityMember> searchEntityMembers(final SearchEntityMemberDescriptor searchDescriptor, final String kind,
            final SearchOptions searchOptions, final String querySuffix) throws SBonitaException {
        EntityMemberSearchEntity transactionContent = new EntityMemberSearchEntity(searchDescriptor, kind, searchOptions, querySuffix);
        transactionContent.execute();
        return transactionContent.getResult();
    }

    class EntityMemberSearchEntityForUser extends ExternalIdentityMappingSearchEntity {

        private final long userId;

        private final String externalId;

        public EntityMemberSearchEntityForUser(final SearchEntityDescriptor searchDescriptor, final String kind, final long userId, final String externalId,
                final SearchOptions options) {
            super(searchDescriptor, kind, options);
            this.userId = userId;
            this.externalId = externalId;
        }

        @Override
        public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
            return serviceAccessor.getExternalIdentityMappingService().getNumberOfExternalIdentityMappingsForUser(kind, userId, externalId, searchOptions,
                    "Involving");
        }

        @Override
        public List<SExternalIdentityMapping> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
            return serviceAccessor.getExternalIdentityMappingService().searchExternalIdentityMappingsForUser(kind, userId, externalId, searchOptions,
                    "Involving");
        }

    }

    class EntityMemberSearchEntity extends ExternalIdentityMappingSearchEntity {

        private final String querySuffix;

        public EntityMemberSearchEntity(final SearchEntityMemberDescriptor searchDescriptor, final String kind, final SearchOptions options,
                final String querySuffix) {
            super(searchDescriptor, kind, options);
            this.querySuffix = querySuffix;
        }

        @Override
        public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
            return serviceAccessor.getExternalIdentityMappingService().getNumberOfExternalIdentityMappings(kind, searchOptions, querySuffix);
        }

        @Override
        public List<SExternalIdentityMapping> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
            return serviceAccessor.getExternalIdentityMappingService().searchExternalIdentityMappings(kind, searchOptions, querySuffix);
        }

    }

}
