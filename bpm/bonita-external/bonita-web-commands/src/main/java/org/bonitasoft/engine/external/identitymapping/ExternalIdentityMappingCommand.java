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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.entitymember.EntityMember;
import org.bonitasoft.engine.entitymember.impl.EntityMemberImpl;
import org.bonitasoft.engine.external.identity.mapping.SExternalIdentityMappingDeletionException;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilder;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilderFactory;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public abstract class ExternalIdentityMappingCommand extends MemberCommand {

    protected static final String ENTITY_MEMBER_ID_KEY = "ENTITY_MEMBER_ID_KEY";

    protected static final String EXTERNAL_ID_KEY = "EXTERNAL_ID_KEY";

    protected static final String DISCRIMINATOR_ID_KEY = "DISCRIMINATOR_ID_KEY";

    protected static final String USER_ID_KEY = "USER_ID_KEY";

    protected static final String ROLE_ID_KEY = "ROLE_ID_KEY";

    protected static final String GROUP_ID_KEY = "GROUP_ID_KEY";

    protected static final String SEARCH_OPTIONS_KEY = "SEARCH_OPTIONS_KEY";

    protected TenantServiceAccessor serviceAccessor;

    public TenantServiceAccessor getServiceAccessor() {
        return serviceAccessor;
    }

    protected SExternalIdentityMapping addExternalIdentityMapping(final String externalId, final long userId, final long roleId, final long groupId,
            final String kind, final MemberType memberType) throws SBonitaException {
        final SExternalIdentityMappingBuilder builder = BuilderFactory.get(SExternalIdentityMappingBuilderFactory.class).createNewInstance(externalId)
                .setGroupId(groupId);
        builder.setKind(kind).setRoleId(roleId).setUserId(userId);
        final CreateExternalIdentityMapping transactionContent = new CreateExternalIdentityMapping(builder, memberType, userId, groupId, roleId);
        transactionContent.execute();
        return transactionContent.getResult();
    }

    protected void removeExternalIdentityMapping(final long sExtIdentityMappingId) throws SBonitaException {
        final RemoveExternalIdentityMapping transactionContent = new RemoveExternalIdentityMapping(sExtIdentityMappingId);
        transactionContent.execute();
    }

    /**
     * Deletes all <code>SExternalIdentityMapping</code> objects associated with the specified externalId and kind.
     * 
     * @param externalId
     *            the external Id identifying the <code>SExternalIdentityMapping</code>s to delete.
     * @param kind
     *            the discriminator of the <code>SExternalIdentityMapping</code>
     * @throws SExternalIdentityMappingDeletionException
     *             in case a deletion problem occurs
     */
    protected void deleteExternalIdentityMappings(final String externalId, final String kind) throws SExternalIdentityMappingDeletionException {
        final DeleteExternalIdentityMappings transactionContent = new DeleteExternalIdentityMappings(kind, externalId);
        try {
            transactionContent.execute();
        } catch (final SBonitaException e) {
            throw new SExternalIdentityMappingDeletionException(e);
        }
    }

    class CreateExternalIdentityMapping implements TransactionContentWithResult<SExternalIdentityMapping> {

        private final SExternalIdentityMappingBuilder builder;

        private final MemberType memberType;

        private final long userId;

        private final long groupId;

        private final long roleId;

        SExternalIdentityMapping mapping;

        CreateExternalIdentityMapping(final SExternalIdentityMappingBuilder builder, final MemberType memberType, final long userId, final long groupId,
                final long roleId) {
            this.builder = builder;
            this.memberType = memberType;
            this.userId = userId;
            this.groupId = groupId;
            this.roleId = roleId;
        }

        @Override
        public void execute() throws SBonitaException {
            setDisplayNames(builder, memberType, userId, groupId, roleId);
            mapping = builder.done();
            serviceAccessor.getExternalIdentityMappingService().createExternalIdentityMapping(mapping);
            // Let's retrieve the created mapping from its id, updated by the persistence service:
            final String querySuffix = getQuerySuffix(memberType);
            mapping = serviceAccessor.getExternalIdentityMappingService().getExternalIdentityMappingById(mapping.getId(), querySuffix, querySuffix);
        }

        @Override
        public SExternalIdentityMapping getResult() {
            return mapping;
        }

    }

    class RemoveExternalIdentityMapping implements TransactionContent {

        private final long mappingId;

        RemoveExternalIdentityMapping(final long mappingId) {
            this.mappingId = mappingId;
        }

        @Override
        public void execute() throws SBonitaException {
            serviceAccessor.getExternalIdentityMappingService().deleteExternalIdentityMapping(mappingId);
        }
    }

    class DeleteExternalIdentityMappings implements TransactionContent {

        private final String kind;

        private final String externalId;

        DeleteExternalIdentityMappings(final String kind, final String externalId) {
            this.kind = kind;
            this.externalId = externalId;
        }

        @Override
        public void execute() throws SBonitaException {
            final List<SExternalIdentityMapping> searchExternalIdentityMappings = serviceAccessor.getExternalIdentityMappingService()
                    .searchExternalIdentityMappings(kind, externalId, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS));
            for (final SExternalIdentityMapping mapping : searchExternalIdentityMappings) {
                serviceAccessor.getExternalIdentityMappingService().deleteExternalIdentityMapping(mapping);
            }
        }
    }

    protected abstract class ExternalIdentityMappingSearchEntity extends AbstractSearchEntity<EntityMember, SExternalIdentityMapping> {

        protected final String kind;

        public ExternalIdentityMappingSearchEntity(final SearchEntityDescriptor searchDescriptor, final String kind, final SearchOptions options) {
            super(searchDescriptor, options);
            this.kind = kind;
        }

        @Override
        public List<EntityMember> convertToClientObjects(final List<SExternalIdentityMapping> serverObjects) {
            return toEntityMembers(serverObjects);
        }

    }

    protected EntityMember toEntityMember(final SExternalIdentityMapping eiMapping) {
        return new EntityMemberImpl(eiMapping.getId(), eiMapping.getExternalId(), eiMapping.getUserId(), eiMapping.getGroupId(), eiMapping.getRoleId(),
                eiMapping.getDisplayNamePart1(), eiMapping.getDisplayNamePart2(), eiMapping.getDisplayNamePart3());
    }

    private List<EntityMember> toEntityMembers(final List<SExternalIdentityMapping> serverObjects) {
        final List<EntityMember> list = new ArrayList<EntityMember>(serverObjects.size());
        for (final SExternalIdentityMapping sMapping : serverObjects) {
            list.add(toEntityMember(sMapping));
        }
        return list;
    }

    private void setDisplayNames(final SExternalIdentityMappingBuilder builder, final MemberType memberType, final long userId, final long groupId,
            final long roleId) throws SUserNotFoundException, SGroupNotFoundException, SRoleNotFoundException {
        switch (memberType) {
            case USER:
                final SUser user = serviceAccessor.getIdentityService().getUser(userId);
                builder.setUserId(userId);
                builder.setDisplayNamePart1(user.getFirstName());
                builder.setDisplayNamePart2(user.getLastName());
                builder.setDisplayNamePart3(user.getUserName());
                break;

            case GROUP:
                SGroup group = serviceAccessor.getIdentityService().getGroup(groupId);
                builder.setGroupId(groupId);
                builder.setDisplayNamePart1(group.getName());
                builder.setDisplayNamePart2(group.getParentPath());
                break;

            case ROLE:
                SRole role = serviceAccessor.getIdentityService().getRole(roleId);
                builder.setRoleId(roleId);
                builder.setDisplayNamePart1(role.getName());
                break;

            case MEMBERSHIP:
                group = serviceAccessor.getIdentityService().getGroup(groupId);
                role = serviceAccessor.getIdentityService().getRole(roleId);
                builder.setGroupId(groupId);
                builder.setRoleId(roleId);
                builder.setDisplayNamePart1(role.getName());
                builder.setDisplayNamePart2(group.getName());
                builder.setDisplayNamePart3(group.getParentPath());
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
