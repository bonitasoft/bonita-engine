/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilderFactory;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedGroup;
import org.bonitasoft.engine.identity.xml.ExportedRole;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ImportOrganizationMergeDuplicatesStrategy implements ImportOrganizationStrategy {

    private final IdentityService identityService;

    private final SCustomUserInfoValueAPI userInfoValueAPI;
    private TechnicalLoggerService logger;

    public ImportOrganizationMergeDuplicatesStrategy(final IdentityService identityService, final SCustomUserInfoValueAPI userInfoValueAPI, TechnicalLoggerService logger) {
        this.identityService = identityService;
        this.userInfoValueAPI = userInfoValueAPI;
        this.logger = logger;
    }

    @Override
    public void foundExistingGroup(final SGroup existingGroup, final ExportedGroup newGroup) throws SIdentityException {
        final EntityUpdateDescriptor descriptor = getGroupDescriptor(existingGroup, newGroup);
        if (!descriptor.getFields().isEmpty()) {
            identityService.updateGroup(existingGroup, descriptor, null);
        }
    }

    @Override
    public void foundExistingUser(final SUser existingUser, final ExportedUser userToImport) throws SBonitaException {
        final long existingUserId = existingUser.getId();
        final EntityUpdateDescriptor descriptor = getUserDescriptor(userToImport);
        identityService.updateUser(existingUser, descriptor, userToImport.isPasswordEncrypted());

        createOrUpdatePersonalContactInfo(userToImport, existingUserId);
        createOrUpdateProfessionalContactInfo(userToImport, existingUserId);
        updateCustomUserInfoValues(userToImport, existingUserId);
    }

    private void updateCustomUserInfoValues(final ExportedUser userToImport, final long existingUserId) throws SBonitaException {
        for (final ExportedCustomUserInfoValue infoValue : userToImport.getCustomUserInfoValues()) {
            final SCustomUserInfoDefinition customUserInfoDefinition = identityService.getCustomUserInfoDefinitionByName(infoValue.getName());
            userInfoValueAPI.set(customUserInfoDefinition.getId(), existingUserId, infoValue.getValue());
        }
    }

    private void createOrUpdateProfessionalContactInfo(final ExportedUser user, final long userId) throws SIdentityException, SUserCreationException {
        SContactInfo professContactInfo = identityService.getUserContactInfo(userId, false);
        if (professContactInfo == null) {
            professContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, false).done();
            identityService.createUserContactInfo(professContactInfo);
        }
        final EntityUpdateDescriptor professionalDataDesc = getUserContactInfoDescriptor(user, false);
        identityService.updateUserContactInfo(professContactInfo, professionalDataDesc);
    }

    private void createOrUpdatePersonalContactInfo(final ExportedUser user, final long userId) throws SIdentityException, SUserCreationException {
        SContactInfo persoContactInfo = identityService.getUserContactInfo(userId, true);
        if (persoContactInfo == null) {
            persoContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, true).done();
            identityService.createUserContactInfo(persoContactInfo);
        }
        final EntityUpdateDescriptor personalDataDesc = getUserContactInfoDescriptor(user, true);
        identityService.updateUserContactInfo(persoContactInfo, personalDataDesc);
    }

    @Override
    public void foundExistingRole(final SRole existingRole, final ExportedRole newRole) throws SIdentityException {
        final EntityUpdateDescriptor descriptor = getRoleDescriptor(existingRole, newRole);
        if (!descriptor.getFields().isEmpty()) {
            identityService.updateRole(existingRole, descriptor, null);
        }
    }

    @Override
    public void foundExistingMembership(final SUserMembership existingMembership) {

    }

    protected EntityUpdateDescriptor getRoleDescriptor(final SRole existingRole, final ExportedRole exportedRole) {
        final SRoleUpdateBuilder roleUpdateBuilder = BuilderFactory.get(SRoleUpdateBuilderFactory.class).createNewInstance();
        final String name = exportedRole.getName();
        if (name != null && !name.equals(existingRole.getName())) {
            roleUpdateBuilder.updateName(name);
        }
        final String description = exportedRole.getDescription();
        if (description != null && !description.equals(existingRole.getDescription())) {
            roleUpdateBuilder.updateDescription(description);
        }
        final String displayName = exportedRole.getDisplayName();
        if (displayName != null && !displayName.equals(existingRole.getDisplayName())) {
            roleUpdateBuilder.updateDisplayName(displayName);
        }
        return roleUpdateBuilder.done();
    }

    protected EntityUpdateDescriptor getGroupDescriptor(final SGroup existingGroup, final ExportedGroup exportedGroup) {
        final SGroupUpdateBuilder groupUpdateBuilder = BuilderFactory.get(SGroupUpdateBuilderFactory.class).createNewInstance();
        final String name = exportedGroup.getName();
        if (name != null && !name.equals(existingGroup.getName())) {
            groupUpdateBuilder.updateName(name);
        }
        final String parentPath = exportedGroup.getParentPath();
        if (parentPath != null && !parentPath.equals(existingGroup.getParentPath())) {
            groupUpdateBuilder.updateName(parentPath);
        }
        final String description = exportedGroup.getDescription();
        if (description != null && !description.equals(existingGroup.getDescription())) {
            groupUpdateBuilder.updateDescription(description);
        }
        final String displayName = exportedGroup.getDisplayName();
        if (displayName != null && !displayName.equals(existingGroup.getDisplayName())) {
            groupUpdateBuilder.updateDisplayName(displayName);
        }
        return groupUpdateBuilder.done();
    }

    protected EntityUpdateDescriptor getUserContactInfoDescriptor(final ExportedUser user, final boolean isPersonal) {
        final SContactInfoUpdateBuilder updateBuilder = BuilderFactory.get(SContactInfoUpdateBuilderFactory.class).createNewInstance();
        if (isPersonal) {
            updateBuilder.updateAddress(user.getPersonalAddress());
            updateBuilder.updateBuilding(user.getPersonalBuilding());
            updateBuilder.updateCity(user.getPersonalCity());
            updateBuilder.updateCountry(user.getPersonalCountry());
            updateBuilder.updateEmail(user.getPersonalEmail());
            updateBuilder.updateFaxNumber(user.getPersonalFaxNumber());
            updateBuilder.updateMobileNumber(user.getPersonalMobileNumber());
            updateBuilder.updatePhoneNumber(user.getPersonalPhoneNumber());
            updateBuilder.updateRoom(user.getPersonalRoom());
            updateBuilder.updateState(user.getPersonalState());
            updateBuilder.updateWebsite(user.getPersonalWebsite());
            updateBuilder.updateZipCode(user.getPersonalZipCode());
        } else {
            updateBuilder.updateAddress(user.getProfessionalAddress());
            updateBuilder.updateBuilding(user.getProfessionalBuilding());
            updateBuilder.updateCity(user.getProfessionalCity());
            updateBuilder.updateCountry(user.getProfessionalCountry());
            updateBuilder.updateEmail(user.getProfessionalEmail());
            updateBuilder.updateFaxNumber(user.getProfessionalFaxNumber());
            updateBuilder.updateMobileNumber(user.getProfessionalMobileNumber());
            updateBuilder.updatePhoneNumber(user.getProfessionalPhoneNumber());
            updateBuilder.updateRoom(user.getProfessionalRoom());
            updateBuilder.updateState(user.getProfessionalState());
            updateBuilder.updateWebsite(user.getProfessionalWebsite());
            updateBuilder.updateZipCode(user.getProfessionalZipCode());
        }
        return updateBuilder.done();
    }

    protected EntityUpdateDescriptor getUserDescriptor(final ExportedUser user) {
        final SUserUpdateBuilder userUpdateBuilder = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance();
        userUpdateBuilder.updateFirstName(user.getFirstName());
        userUpdateBuilder.updateJobTitle(user.getJobTitle());
        userUpdateBuilder.updateLastName(user.getLastName());
        userUpdateBuilder.updatePassword(user.getPassword());
        userUpdateBuilder.updateTitle(user.getTitle());
        userUpdateBuilder.updateUserName(user.getUserName());
        userUpdateBuilder.updateEnabled(user.isEnabled());
        return userUpdateBuilder.done();
    }

    @Override
    public void foundExistingCustomUserInfoDefinition(final SCustomUserInfoDefinition existingUserInfoDefinition,
                                                      final ExportedCustomUserInfoDefinition newUserInfoDefinition) throws SIdentityException {
        // only description is updated as it only matches if they have the same name
        final EntityUpdateDescriptor updateDescriptor = getUpdateDescriptor(newUserInfoDefinition.getDescription());
        identityService.updateCustomUserInfoDefinition(existingUserInfoDefinition, updateDescriptor);
    }

    private EntityUpdateDescriptor getUpdateDescriptor(final String newDescription) {
        final SCustomUserInfoDefinitionUpdateBuilder builder = BuilderFactory.get(SCustomUserInfoDefinitionUpdateBuilderFactory.class).createNewInstance();
        builder.updateDescription(newDescription);
        return builder.done();
    }

}
