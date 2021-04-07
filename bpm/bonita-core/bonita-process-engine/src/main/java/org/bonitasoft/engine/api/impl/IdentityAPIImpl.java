/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActor;
import org.bonitasoft.engine.api.impl.transaction.identity.AddUserMembership;
import org.bonitasoft.engine.api.impl.transaction.identity.AddUserMemberships;
import org.bonitasoft.engine.api.impl.transaction.identity.DeleteGroup;
import org.bonitasoft.engine.api.impl.transaction.identity.DeleteGroups;
import org.bonitasoft.engine.api.impl.transaction.identity.DeleteRole;
import org.bonitasoft.engine.api.impl.transaction.identity.DeleteRoles;
import org.bonitasoft.engine.api.impl.transaction.identity.DeleteUser;
import org.bonitasoft.engine.api.impl.transaction.identity.DeleteUsers;
import org.bonitasoft.engine.api.impl.transaction.identity.GetGroups;
import org.bonitasoft.engine.api.impl.transaction.identity.GetNumberOfInstance;
import org.bonitasoft.engine.api.impl.transaction.identity.GetNumberOfUserMemberships;
import org.bonitasoft.engine.api.impl.transaction.identity.GetNumberOfUsersInType;
import org.bonitasoft.engine.api.impl.transaction.identity.GetRoles;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSContactInfo;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.identity.GetUserMembership;
import org.bonitasoft.engine.api.impl.transaction.identity.GetUserMembershipsOfGroup;
import org.bonitasoft.engine.api.impl.transaction.identity.GetUserMembershipsOfRole;
import org.bonitasoft.engine.api.impl.transaction.identity.UpdateGroup;
import org.bonitasoft.engine.api.impl.transaction.identity.UpdateMembershipByRoleIdAndGroupId;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.InvalidGroupNameException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.engine.identity.ContactDataUpdater;
import org.bonitasoft.engine.identity.ContactDataUpdater.ContactDataField;
import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.ExportOrganization;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.GroupNotFoundException;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.engine.identity.GroupUpdater.GroupField;
import org.bonitasoft.engine.identity.Icon;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.ImportOrganization;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.InvalidOrganizationFileFormatException;
import org.bonitasoft.engine.identity.MembershipNotFoundException;
import org.bonitasoft.engine.identity.OrganizationExportException;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.RoleNotFoundException;
import org.bonitasoft.engine.identity.RoleUpdater;
import org.bonitasoft.engine.identity.RoleUpdater.RoleField;
import org.bonitasoft.engine.identity.SGroupCreationException;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SIcon;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.UserMembershipCriterion;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.identity.UserUpdater.UserField;
import org.bonitasoft.engine.identity.UserWithContactData;
import org.bonitasoft.engine.identity.impl.UserWithContactDataImpl;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.identity.SearchGroups;
import org.bonitasoft.engine.search.identity.SearchRoles;
import org.bonitasoft.engine.search.identity.SearchUsers;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Zhang Bole
 * @author Yanyan Liu
 * @author Lu Kai
 * @author Hongwen Zang
 * @author Celine Souchet
 */
@AvailableWhenTenantIsPaused
public class IdentityAPIImpl implements IdentityAPI {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public User createUser(final String userName, final String password)
            throws AlreadyExistsException, CreationException {
        final UserCreator creator = new UserCreator(userName, password);
        creator.setEnabled(true);
        return createUser(creator);
    }

    @Override
    public User createUser(final String userName, final String password, final String firstName, final String lastName)
            throws AlreadyExistsException,
            CreationException {
        final UserCreator creator = new UserCreator(userName, password);
        creator.setFirstName(firstName).setLastName(lastName);
        creator.setEnabled(true);
        return createUser(creator);
    }

    @Override
    public User createUser(final UserCreator creator) throws CreationException {
        validateUserCreator(creator);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        IdentityService identityService = tenantAccessor.getIdentityService();
        if (creator.getFields().containsKey(UserCreator.UserField.ICON_NAME)
                || creator.getFields().containsKey(UserCreator.UserField.ICON_PATH)) {
            tenantAccessor.getTechnicalLoggerService().log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                    "setIconName and setIconPath are deprecated, use setIcon instead");
        }
        final SUser sUser = ModelConvertor.constructSUser(creator);
        final SContactInfo personalContactInfo = ModelConvertor.constructSUserContactInfo(creator, sUser.getId(), true);
        final SContactInfo proContactInfo = ModelConvertor.constructSUserContactInfo(creator, sUser.getId(), false);
        try {
            SUser user = identityService.createUser(sUser, personalContactInfo, proContactInfo,
                    (String) creator.getFields().get(UserCreator.UserField.ICON_FILENAME),
                    (byte[]) creator.getFields().get(UserCreator.UserField.ICON_CONTENT));
            return ModelConvertor.toUser(user);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    private void validateUserCreator(UserCreator creator) throws CreationException {
        if (creator == null) {
            throw new CreationException("Can not create a null user.");
        }
        final Map<UserCreator.UserField, Serializable> fields = creator.getFields();
        final String userName = (String) fields.get(UserCreator.UserField.NAME);
        if (userName == null || userName.trim().isEmpty()) {
            throw new CreationException("The user name cannot be null or empty.");
        }
        final String password = (String) fields.get(UserCreator.UserField.PASSWORD);
        if (password == null || password.trim().isEmpty()) {
            throw new CreationException("The password cannot be null or empty.");
        }
        try {
            getUserByUserName(userName);
            throw new AlreadyExistsException("A user with name \"" + userName + "\" already exists");
        } catch (final UserNotFoundException ignored) {
        }
    }

    @Override
    public User updateUser(final long userId, final UserUpdater updater) throws UserNotFoundException, UpdateException {
        if (updater == null || !updater.hasFields()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final IdentityService identityService = tenantAccessor.getIdentityService();

        // User change
        final EntityUpdateDescriptor userUpdateDescriptor = getUserUpdateDescriptor(updater,
                tenantAccessor.getTechnicalLoggerService());
        // Personal data change
        final EntityUpdateDescriptor personalDataUpdateDescriptor = getUserContactInfoUpdateDescriptor(
                updater.getPersoContactUpdater());
        // Professional data change
        final EntityUpdateDescriptor professionalDataUpdateDescriptor = getUserContactInfoUpdateDescriptor(
                updater.getProContactUpdater());
        final EntityUpdateDescriptor iconUpdater = getIconUpdater(updater);
        try {
            SUser sUser = identityService.updateUser(userId, userUpdateDescriptor, personalDataUpdateDescriptor,
                    professionalDataUpdateDescriptor, iconUpdater);
            return ModelConvertor.toUser(sUser);
        } catch (final SUserNotFoundException e) {
            throw new UserNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    private EntityUpdateDescriptor getIconUpdater(UserUpdater updater) {
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        if (updater.getFields().containsKey(UserField.ICON_CONTENT)) {
            entityUpdateDescriptor.addField("filename", updater.getFields().get(UserField.ICON_FILENAME));
            entityUpdateDescriptor.addField("content", updater.getFields().get(UserField.ICON_CONTENT));
        }
        return entityUpdateDescriptor;
    }

    private EntityUpdateDescriptor getIconUpdater(GroupUpdater updater) {
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        if (updater.getFields().containsKey(GroupField.ICON_CONTENT)) {
            entityUpdateDescriptor.addField("filename", updater.getFields().get(GroupField.ICON_FILENAME));
            entityUpdateDescriptor.addField("content", updater.getFields().get(GroupField.ICON_CONTENT));
        }
        return entityUpdateDescriptor;
    }

    private EntityUpdateDescriptor getIconUpdater(RoleUpdater updater) {
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        if (updater.getFields().containsKey(RoleUpdater.RoleField.ICON_CONTENT)) {
            entityUpdateDescriptor.addField("filename", updater.getFields().get(RoleUpdater.RoleField.ICON_FILENAME));
            entityUpdateDescriptor.addField("content", updater.getFields().get(RoleUpdater.RoleField.ICON_CONTENT));
        }
        return entityUpdateDescriptor;
    }

    private EntityUpdateDescriptor getUserUpdateDescriptor(final UserUpdater updateDescriptor,
            TechnicalLoggerService technicalLoggerService) {
        final SUserUpdateBuilder userUpdateBuilder = BuilderFactory.get(SUserUpdateBuilderFactory.class)
                .createNewInstance();
        if (updateDescriptor != null) {
            final Map<UserField, Serializable> fields = updateDescriptor.getFields();
            for (final Entry<UserField, Serializable> field : fields.entrySet()) {
                switch (field.getKey()) {
                    case USER_NAME:
                        userUpdateBuilder.updateUserName((String) field.getValue());
                        break;
                    case PASSWORD:
                        userUpdateBuilder.updatePassword((String) field.getValue());
                        break;
                    case FIRST_NAME:
                        userUpdateBuilder.updateFirstName((String) field.getValue());
                        break;
                    case LAST_NAME:
                        userUpdateBuilder.updateLastName((String) field.getValue());
                        break;
                    case MANAGER_ID:
                        userUpdateBuilder.updateManagerUserId((Long) field.getValue());
                        break;
                    case ICON_NAME:
                        technicalLoggerService.log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                                "setIconName is deprecated, use setIcon instead");
                        break;
                    case ICON_PATH:
                        technicalLoggerService.log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                                "setIconPath is deprecated, use setIcon instead");
                        break;
                    case TITLE:
                        userUpdateBuilder.updateTitle((String) field.getValue());
                        break;
                    case JOB_TITLE:
                        userUpdateBuilder.updateJobTitle((String) field.getValue());
                        break;
                    case ENABLED:
                        userUpdateBuilder.updateEnabled((Boolean) field.getValue());
                        break;
                    case ICON_FILENAME:
                    case ICON_CONTENT:
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
            userUpdateBuilder.updateLastUpdate(System.currentTimeMillis());
            return userUpdateBuilder.done();
        }
        return null;
    }

    private EntityUpdateDescriptor getUserContactInfoUpdateDescriptor(final ContactDataUpdater updater) {
        final SContactInfoUpdateBuilder updateBuilder = BuilderFactory.get(SContactInfoUpdateBuilderFactory.class)
                .createNewInstance();
        if (updater != null) {
            final Map<ContactDataField, Serializable> fields = updater.getFields();
            for (final Entry<ContactDataField, Serializable> field : fields.entrySet()) {
                switch (field.getKey()) {
                    case EMAIL:
                        updateBuilder.updateEmail((String) field.getValue());
                        break;
                    case PHONE:
                        updateBuilder.updatePhoneNumber((String) field.getValue());
                        break;
                    case MOBILE:
                        updateBuilder.updateMobileNumber((String) field.getValue());
                        break;
                    case FAX:
                        updateBuilder.updateFaxNumber((String) field.getValue());
                        break;
                    case BUILDING:
                        updateBuilder.updateBuilding((String) field.getValue());
                        break;
                    case ROOM:
                        updateBuilder.updateRoom((String) field.getValue());
                        break;
                    case ADDRESS:
                        updateBuilder.updateAddress((String) field.getValue());
                        break;
                    case ZIP_CODE:
                        updateBuilder.updateZipCode((String) field.getValue());
                        break;
                    case CITY:
                        updateBuilder.updateCity((String) field.getValue());
                        break;
                    case STATE:
                        updateBuilder.updateState((String) field.getValue());
                        break;
                    case COUNTRY:
                        updateBuilder.updateCountry((String) field.getValue());
                        break;
                    case WEBSITE:
                        updateBuilder.updateWebsite((String) field.getValue());
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
            return updateBuilder.done();
        }
        return null;
    }

    @Override
    public void deleteUser(final long userId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final ProfileService profileService = tenantAccessor.getProfileService();
        try {
            final DeleteUser deleteUser = new DeleteUser(identityService, actorMappingService, profileService, userId);
            deleteUser.execute();
            final Set<Long> removedActorIds = deleteUser.getRemovedActorIds();
            updateActorProcessDependencies(tenantAccessor, actorMappingService, removedActorIds);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void deleteUser(final String userName) throws DeletionException {
        if (userName == null) {
            throw new DeletionException("User name can not be null!");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final ProfileService profileService = tenantAccessor.getProfileService();
        try {
            final DeleteUser deleteUser = new DeleteUser(identityService, actorMappingService, profileService,
                    userName);
            deleteUser.execute();
            final Set<Long> removedActorIds = deleteUser.getRemovedActorIds();
            updateActorProcessDependencies(tenantAccessor, actorMappingService, removedActorIds);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void deleteUsers(final List<Long> userIds) throws DeletionException {
        if (userIds != null && !userIds.isEmpty()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final IdentityService identityService = tenantAccessor.getIdentityService();
            final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
            final ProfileService profileService = tenantAccessor.getProfileService();
            try {
                final DeleteUsers deleteUsers = new DeleteUsers(identityService, actorMappingService, profileService,
                        userIds);
                deleteUsers.execute();
            } catch (final SBonitaException sbe) {
                throw new DeletionException(sbe);
            }
        }
    }

    @Override
    public User getUser(final long userId) throws UserNotFoundException {
        if (userId == -1) {
            throw new UserNotFoundException("The technical user is not a usable user");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetSUser transactionContent = new GetSUser(identityService, userId);
            transactionContent.execute();
            return ModelConvertor.toUser(transactionContent.getResult());
        } catch (final SUserNotFoundException sunfe) {
            throw new UserNotFoundException(sunfe);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public User getUserByUserName(final String userName) throws UserNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetSUser transactionContent = new GetSUser(identityService, userName);
            transactionContent.execute();
            return ModelConvertor.toUser(transactionContent.getResult());
        } catch (final SUserNotFoundException sunfe) {
            throw new UserNotFoundException(sunfe);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public UserWithContactData getUserWithProfessionalDetails(final long userId) throws UserNotFoundException {
        final User user = getUser(userId);
        final ContactData contactData = getUserContactData(userId, false);
        return new UserWithContactDataImpl(user, contactData);
    }

    @Override
    public ContactData getUserContactData(final long userId, final boolean personal) throws UserNotFoundException {
        if (userId == -1) {
            throw new UserNotFoundException("The technical user is not a usable user");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetSContactInfo txContent = new GetSContactInfo(userId, identityService, personal);
            txContent.execute();
            final SContactInfo result = txContent.getResult();
            if (result == null) {
                return null;
            }
            return ModelConvertor.toUserContactData(result);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public long getNumberOfUsers() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetNumberOfInstance transactionContent = new GetNumberOfInstance("getNumberOfUsers", identityService);
            transactionContent.execute();
            return transactionContent.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    // FIXME rewrite ME!!!
    public List<User> getUsers(final int startIndex, final int maxResults, final UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final IdentityService identityService = tenantAccessor.getIdentityService();
            return getUsersWithOrder(startIndex, maxResults, criterion, identityService);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public Map<Long, User> getUsers(final List<Long> userIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final Map<Long, User> users = new HashMap<>();
        try {
            final List<SUser> sUsers = identityService.getUsers(userIds);
            for (final SUser sUser : sUsers) {
                users.put(sUser.getId(), ModelConvertor.toUser(sUser));
            }
            return users;
        } catch (final SUserNotFoundException sunfe) {
            throw new RetrieveException(sunfe);
        }
    }

    @Override
    public Map<String, User> getUsersByUsernames(final List<String> userNames) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final Map<String, User> users = new HashMap<>();
        try {
            final List<SUser> sUsers = identityService.getUsersByUsername(userNames);
            for (final SUser sUser : sUsers) {
                users.put(sUser.getUserName(), ModelConvertor.toUser(sUser));
            }
            return users;
        } catch (final SIdentityException sunfe) {
            throw new RetrieveException(sunfe);
        }
    }

    @Override
    public SearchResult<User> searchUsers(final SearchOptions options) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchUsers searchUsers = new SearchUsers(identityService,
                searchEntitiesDescriptor.getSearchUserDescriptor(), options);
        try {
            searchUsers.execute();
            return searchUsers.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public long getNumberOfUsersInRole(final long roleId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetNumberOfUsersInType transactionContentWithResult = new GetNumberOfUsersInType(roleId,
                    "getNumberOfUsersInRole", identityService);
            transactionContentWithResult.execute();
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getUsersInRole(final long roleId, final int startIndex, final int maxResults,
            final UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(
                    identityService.getUsersWithRole(roleId, startIndex, maxResults, sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getActiveUsersInRole(long roleId, int startIndex, int maxResults, UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(identityService.getActiveUsersWithRole(roleId, startIndex, maxResults,
                    sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getInactiveUsersInRole(long roleId, int startIndex, int maxResults, UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(identityService.getInactiveUsersWithRole(roleId, startIndex, maxResults,
                    sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getUsersWithManager(long managerId, int startIndex, int maxResults, UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(identityService.getUsersWithManager(managerId, startIndex, maxResults,
                    sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getActiveUsersWithManager(long managerId, int startIndex, int maxResults,
            UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(identityService.getActiveUsersWithManager(managerId, startIndex, maxResults,
                    sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getInactiveUsersWithManager(long managerId, int startIndex, int maxResults,
            UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(identityService.getInactiveUsersWithManager(managerId, startIndex, maxResults,
                    sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public long getNumberOfUsersInGroup(final long groupId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetNumberOfUsersInType transactionContentWithResult = new GetNumberOfUsersInType(groupId,
                "getNumberOfUsersInGroup", identityService);
        try {
            transactionContentWithResult.execute();
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getUsersInGroup(final long groupId, final int startIndex, final int maxResults,
            final UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor
                    .toUsers(identityService.getUsersInGroup(groupId, startIndex, maxResults, sort.getField(),
                            sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<User> getActiveUsersInGroup(long groupId, int startIndex, int maxResults, UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor
                    .toUsers(identityService.getActiveUsersInGroup(groupId, startIndex, maxResults, sort.getField(),
                            sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }

    }

    @Override
    public List<User> getInactiveUsersInGroup(long groupId, int startIndex, int maxResults, UserCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        Sort sort = getSortFromCriterion(criterion);
        try {
            return ModelConvertor.toUsers(identityService.getInactiveUsersInGroup(groupId, startIndex, maxResults,
                    sort.getField(), sort.getOrder()));
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<Long> getUserIdsWithCustomUserInfo(String infoName, String infoValue, boolean usePartialMatch,
            int startIndex, int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            return identityService.getUserIdsWithCustomUserInfo(infoName, infoValue, usePartialMatch, startIndex,
                    maxResults);
        } catch (SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Role createRole(final String roleName) throws AlreadyExistsException, CreationException {
        return createRole(new RoleCreator(roleName));
    }

    @Override
    public Role createRole(final RoleCreator creator) throws AlreadyExistsException, CreationException {
        if (creator == null) {
            throw new CreationException("Unable to create a role with a null RoleCreator object");
        }
        if (creator.getFields().get(org.bonitasoft.engine.identity.RoleCreator.RoleField.NAME) == null) {
            throw new CreationException("Unable to create a role with a null name");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        if (creator.getFields().containsKey(RoleCreator.RoleField.ICON_NAME)
                || creator.getFields().containsKey(RoleCreator.RoleField.ICON_PATH)) {
            tenantAccessor.getTechnicalLoggerService().log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                    "setIconName and setIconPath are deprecated, use setIcon instead");
        }
        final SRole sRole = ModelConvertor.constructSRole(creator);
        try {
            getRoleByName(sRole.getName());
            throw new AlreadyExistsException("A role named \"" + sRole.getName() + "\" already exists");
        } catch (final RoleNotFoundException ignored) {
            // Ok, role can now be created.
        }
        try {
            identityService.createRole(sRole,
                    (String) creator.getFields().get(RoleCreator.RoleField.ICON_FILENAME),
                    (byte[]) creator.getFields().get(RoleCreator.RoleField.ICON_CONTENT));
            return ModelConvertor.toRole(sRole);
        } catch (final SIdentityException e) {
            throw new CreationException("Role create exception!", e);
        }
    }

    @Override
    public Role updateRole(final long roleId, final RoleUpdater updateDescriptor)
            throws RoleNotFoundException, UpdateException {
        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final EntityUpdateDescriptor changeDescriptor = getRoleUpdateDescriptor(updateDescriptor,
                    tenantAccessor.getTechnicalLoggerService());
            return ModelConvertor.toRole(identityService.updateRole(identityService.getRole(roleId), changeDescriptor,
                    getIconUpdater(updateDescriptor)));
        } catch (final SRoleNotFoundException e) {
            throw new RoleNotFoundException(e);
        } catch (SIdentityException e) {
            throw new UpdateException(e);
        }
    }

    private EntityUpdateDescriptor getRoleUpdateDescriptor(final RoleUpdater updateDescriptor,
            TechnicalLoggerService technicalLoggerService) {
        final SRoleUpdateBuilder roleUpdateBuilder = BuilderFactory.get(SRoleUpdateBuilderFactory.class)
                .createNewInstance();
        final Map<RoleField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<RoleField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    roleUpdateBuilder.updateName((String) field.getValue());
                    break;
                case DISPLAY_NAME:
                    roleUpdateBuilder.updateDisplayName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    roleUpdateBuilder.updateDescription((String) field.getValue());
                    break;
                case ICON_NAME:
                    technicalLoggerService.log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                            "setIconPath is deprecated, use setIcon instead");
                    break;
                case ICON_PATH:
                    technicalLoggerService.log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                            "setIconPath is deprecated, use setIcon instead");
                    break;
                default:
                    break;
            }
        }
        roleUpdateBuilder.updateLastUpdate(System.currentTimeMillis());
        return roleUpdateBuilder.done();
    }

    @Override
    public void deleteRole(final long roleId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();

        final ProfileService profileService = tenantAccessor.getProfileService();
        final DeleteRole deleteRole = new DeleteRole(identityService, actorMappingService, profileService, roleId);
        try {
            deleteRole.execute();
            final Set<Long> removedActorIds = deleteRole.getRemovedActorIds();
            updateActorProcessDependencies(tenantAccessor, actorMappingService, removedActorIds);
        } catch (final SRoleNotFoundException ignored) {
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void deleteRoles(final List<Long> roleIds) throws DeletionException {
        try {
            NullCheckingUtil.checkArgsNotNull(roleIds);
        } catch (final IllegalArgumentException e) {
            throw new DeletionException(e);
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final DeleteRoles deleteRoles = new DeleteRoles(identityService, actorMappingService, profileService, roleIds);
        try {
            deleteRoles.execute();
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public Role getRole(final long roleId) throws RoleNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            return ModelConvertor.toRole(identityService.getRole(roleId));
        } catch (SRoleNotFoundException e) {
            throw new RoleNotFoundException(e);
        }
    }

    @Override
    public Role getRoleByName(final String roleName) throws RoleNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            return ModelConvertor.toRole(identityService.getRoleByName(roleName));
        } catch (final SRoleNotFoundException e) {
            throw new RoleNotFoundException(e);
        }
    }

    @Override
    public long getNumberOfRoles() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetNumberOfInstance getNumberOfInstance = new GetNumberOfInstance("getNumberOfRoles",
                    identityService);
            getNumberOfInstance.execute();
            return getNumberOfInstance.getResult();
        } catch (final SBonitaException e) {
            return 0;
        }
    }

    @Override
    public List<Role> getRoles(final int startIndex, final int maxResults, final RoleCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        String field = null;
        OrderByType order = null;
        switch (criterion) {
            case NAME_ASC:
                field = SRole.NAME;
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = SRole.NAME;
                order = OrderByType.DESC;
                break;
            case DISPLAY_NAME_ASC:
                field = SRole.DISPLAY_NAME;
                order = OrderByType.ASC;
                break;
            case DISPLAY_NAME_DESC:
                field = SRole.DISPLAY_NAME;
                order = OrderByType.DESC;
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            final GetRoles getRolesWithOrder = new GetRoles(identityService, startIndex, maxResults, field, order);
            getRolesWithOrder.execute();
            return ModelConvertor.toRoles(getRolesWithOrder.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Map<Long, Role> getRoles(final List<Long> roleIds) {
        final Map<Long, Role> roles = new HashMap<>();
        for (final Long roleId : roleIds) {
            try {
                final Role role = getRole(roleId);
                roles.put(roleId, role);
            } catch (final RoleNotFoundException e) {
                // if the role does not exist; skip the role
            }
        }
        return roles;
    }

    @Override
    public SearchResult<Role> searchRoles(final SearchOptions options) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchRoles searchRoles = new SearchRoles(identityService,
                searchEntitiesDescriptor.getSearchRoleDescriptor(), options);
        try {
            searchRoles.execute();
            return searchRoles.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public Group createGroup(final String name, final String parentPath) throws CreationException {
        final GroupCreator groupCreator = new GroupCreator(name);
        groupCreator.setParentPath(parentPath);
        return createGroup(groupCreator);
    }

    @Override
    public Group createGroup(final GroupCreator creator) throws CreationException {
        if (creator == null) {
            throw new CreationException("Cannot create a null group");
        }
        String groupName = creator.getFields().get(GroupCreator.GroupField.NAME).toString();
        if (groupName.contains("/")) {
            throw new InvalidGroupNameException("Cannot create a group with '/' in its name");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        if (creator.getFields().containsKey(GroupCreator.GroupField.ICON_NAME)
                || creator.getFields().containsKey(GroupCreator.GroupField.ICON_PATH)) {
            tenantAccessor.getTechnicalLoggerService().log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                    "setIconName and setIconPath are deprecated, use setIcon instead");
        }
        final SGroup sGroup = ModelConvertor.constructSGroup(creator);
        try {
            identityService.getGroupByPath(sGroup.getPath());
            throw new AlreadyExistsException("Group named \"" + sGroup.getName() + "\" already exists");
        } catch (final SGroupNotFoundException ignored) {

        }
        try {
            identityService.createGroup(sGroup,
                    (String) creator.getFields().get(GroupCreator.GroupField.ICON_FILENAME),
                    (byte[]) creator.getFields().get(GroupCreator.GroupField.ICON_CONTENT));
        } catch (SGroupCreationException e) {
            throw new CreationException(e);
        }
        return ModelConvertor.toGroup(sGroup);
    }

    @Override
    public Group updateGroup(final long groupId, final GroupUpdater updater)
            throws GroupNotFoundException, UpdateException, AlreadyExistsException {
        if (updater == null || updater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            checkPathUnicity(groupId, updater, identityService);
            final EntityUpdateDescriptor changeDescriptor = getGroupUpdateDescriptor(updater,
                    tenantAccessor.getTechnicalLoggerService());
            return ModelConvertor.toGroup(
                    new UpdateGroup(groupId, changeDescriptor, identityService, getIconUpdater(updater)).update());
        } catch (final SGroupNotFoundException e) {
            throw new GroupNotFoundException(e);
        } catch (final SIdentityException sbe) {
            throw new UpdateException(sbe);
        }
    }

    private void checkPathUnicity(long groupId, GroupUpdater updater, IdentityService identityService)
            throws SGroupNotFoundException, AlreadyExistsException {
        final Serializable updatedName = updater.getFields().get(GroupField.NAME);
        SGroup sGroupToBeUpdated = identityService.getGroup(groupId);
        String name = updatedName != null ? updatedName.toString() : sGroupToBeUpdated.getName();
        StringBuilder sb = new StringBuilder();
        String parentPath = updater.getFields().get(GroupField.PARENT_PATH) != null
                ? updater.getFields().get(GroupField.PARENT_PATH).toString() : "";
        sb.append(parentPath).append("/").append(name);
        try {
            if (updatedName != null) {
                SGroup group = identityService.getGroupByPath(sb.toString());
                if (group.getId() != groupId) {
                    throw new AlreadyExistsException("Group named \"" + name + "\" already exists");
                }
            }
        } catch (final SGroupNotFoundException e) {
        }
    }

    private EntityUpdateDescriptor getGroupUpdateDescriptor(final GroupUpdater updateDescriptor,
            TechnicalLoggerService technicalLoggerService)
            throws UpdateException {
        final SGroupUpdateBuilder groupUpdateBuilder = BuilderFactory.get(SGroupUpdateBuilderFactory.class)
                .createNewInstance();
        final Map<GroupField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<GroupField, Serializable> field : fields.entrySet()) {
            final Serializable value = field.getValue();
            switch (field.getKey()) {
                case NAME:
                    groupUpdateBuilder.updateName((String) value);
                    break;
                case DISPLAY_NAME:
                    groupUpdateBuilder.updateDisplayName((String) value);
                    break;
                case DESCRIPTION:
                    groupUpdateBuilder.updateDescription((String) value);
                    break;
                case ICON_NAME:
                    technicalLoggerService.log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                            "updateIconName is deprecated, use updateIcon instead");
                    break;
                case ICON_PATH:
                    technicalLoggerService.log(IdentityAPIImpl.class, TechnicalLogSeverity.WARNING,
                            "updateIconPath is deprecated, use updateIcon instead");
                    break;
                case ICON_CONTENT:
                    break;
                case ICON_FILENAME:
                    break;
                case PARENT_PATH:
                    groupUpdateBuilder
                            .updateParentPath((value != null && ((String) value).isEmpty()) ? null : (String) value);
                    break;
                default:
                    throw new UpdateException("Invalid field: " + field.getKey().name());
            }
        }
        groupUpdateBuilder.updateLastUpdate(System.currentTimeMillis());
        return groupUpdateBuilder.done();
    }

    @Override
    public void deleteGroup(final long groupId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final DeleteGroup deleteGroup = new DeleteGroup(identityService, actorMappingService, profileService, groupId);
        try {
            deleteGroup.execute();
            updateActorProcessDependencies(tenantAccessor, actorMappingService, deleteGroup.getRemovedActorIds());
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void deleteGroups(final List<Long> groupIds) throws DeletionException {
        if (groupIds == null) {
            throw new IllegalArgumentException("the list of groups is null");
        }
        if (!groupIds.isEmpty()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final IdentityService identityService = tenantAccessor.getIdentityService();
            final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
            final ProfileService profileService = tenantAccessor.getProfileService();
            try {
                final DeleteGroups deleteGroups = new DeleteGroups(identityService, actorMappingService, profileService,
                        groupIds);
                deleteGroups.execute();
                updateActorProcessDependencies(tenantAccessor, actorMappingService, deleteGroups.getRemovedActorIds());
            } catch (final SBonitaException e) {
                throw new DeletionException(e);
            }
        }
    }

    @Override
    public Group getGroup(final long groupId) throws GroupNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            return ModelConvertor.toGroup(tenantAccessor.getIdentityService().getGroup(groupId));
        } catch (final SGroupNotFoundException e) {
            throw new GroupNotFoundException(e);
        }
    }

    @Override
    public Group getGroupByPath(final String groupPath) throws GroupNotFoundException {
        try {
            return ModelConvertor.toGroup(getTenantAccessor().getIdentityService().getGroupByPath(groupPath));
        } catch (final SGroupNotFoundException e) {
            throw new GroupNotFoundException(e);
        }
    }

    @Override
    public long getNumberOfGroups() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetNumberOfInstance getNumberOfInstance = new GetNumberOfInstance("getNumberOfGroups",
                    identityService);
            getNumberOfInstance.execute();
            return getNumberOfInstance.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public Map<Long, Group> getGroups(final List<Long> groupIds) {
        final Map<Long, Group> groups = new HashMap<>();
        for (final Long groupId : groupIds) {
            try {
                final Group group = getGroup(groupId);
                groups.put(groupId, group);
            } catch (final GroupNotFoundException e) {
                // if the group does not exist; skip the group
            }
        }
        return groups;
    }

    @Override
    public List<Group> getGroups(final int startIndex, final int maxResults, final GroupCriterion pagingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {
            case NAME_ASC:
                field = SGroup.NAME;
                order = OrderByType.ASC;
                break;
            case LABEL_ASC:
                field = SGroup.DISPLAY_NAME;
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = SGroup.NAME;
                order = OrderByType.DESC;
                break;
            case LABEL_DESC:
                field = SGroup.DISPLAY_NAME;
                order = OrderByType.DESC;
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            final GetGroups getGroups = new GetGroups(identityService, startIndex, maxResults, order, field);
            getGroups.execute();
            return ModelConvertor.toGroups(getGroups.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<Group> searchGroups(final SearchOptions options) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchGroups searchGroups = new SearchGroups(identityService,
                searchEntitiesDescriptor.getSearchGroupDescriptor(), options);
        try {
            searchGroups.execute();
            return searchGroups.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    /**
     * Check / update process resolution information, for all processes in a list of actor IDs.
     */
    private void updateActorProcessDependencies(final TenantServiceAccessor tenantAccessor,
            final ActorMappingService actorMappingService,
            final Set<Long> removedActorIds) throws SBonitaException {
        final Set<Long> processDefinitionIds = new HashSet<>(removedActorIds.size());
        for (final Long actorId : removedActorIds) {
            final GetActor getActor = new GetActor(actorMappingService, actorId);
            getActor.execute();
            final SActor actor = getActor.getResult();
            final Long processDefId = actor.getScopeId();
            if (!processDefinitionIds.contains(processDefId)) {
                processDefinitionIds.add(processDefId);
                tenantAccessor.getBusinessArchiveArtifactsManager().resolveDependencies(actor.getScopeId(),
                        tenantAccessor);
            }
        }
    }

    private List<User> getUsersWithOrder(final int startIndex, final int maxResults,
            final UserCriterion pagingCriterion, final IdentityService identityService)
            throws SIdentityException {
        final String field = getUserFieldKey(pagingCriterion);
        final OrderByType order = getUserOrderByType(pagingCriterion);
        if (field == null) {
            return ModelConvertor.toUsers(identityService.getUsers(startIndex, maxResults));
        }
        return ModelConvertor.toUsers(identityService.getUsers(startIndex, maxResults, field, order));
    }

    private OrderByType getUserOrderByType(final UserCriterion pagingCriterion) {
        OrderByType order = null;
        switch (pagingCriterion) {
            case USER_NAME_ASC:
                order = OrderByType.ASC;
                break;
            case FIRST_NAME_ASC:
                order = OrderByType.ASC;
                break;
            case LAST_NAME_ASC:
                order = OrderByType.ASC;
                break;
            case FIRST_NAME_DESC:
                order = OrderByType.DESC;
                break;
            case LAST_NAME_DESC:
                order = OrderByType.DESC;
                break;
            case USER_NAME_DESC:
                order = OrderByType.DESC;
                break;
            default:
                throw new IllegalStateException();
        }
        return order;
    }

    private String getUserFieldKey(final UserCriterion pagingCriterion) {
        String field = null;
        switch (pagingCriterion) {
            case USER_NAME_ASC:
                field = SUser.USER_NAME;
                break;
            case FIRST_NAME_ASC:
                field = SUser.FIRST_NAME;
                break;
            case LAST_NAME_ASC:
                field = SUser.LAST_NAME;
                break;
            case FIRST_NAME_DESC:
                field = SUser.FIRST_NAME;
                break;
            case LAST_NAME_DESC:
                field = SUser.LAST_NAME;
                break;
            case USER_NAME_DESC:
                field = SUser.USER_NAME;
                break;
            default:
                throw new IllegalStateException();
        }
        return field;
    }

    @Override
    public UserMembership addUserMembership(final long userId, final long groupId, final long roleId)
            throws AlreadyExistsException, CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final long assignedBy = SessionInfos.getUserIdFromSession();
        try {
            final GetUserMembership getUserMembership = new GetUserMembership(userId, groupId, roleId, identityService);
            getUserMembership.execute();
            if (getUserMembership.getResult() != null) {
                throw new AlreadyExistsException("A userMembership with userId \"" + userId + "\", groupId \"" + groupId
                        + "\" and roleId \"" + roleId
                        + "\" already exists");
            }
        } catch (final SBonitaException e) {
            // Membership does not exists but was unable to be created
        }
        try {
            final AddUserMembership createUserMembership = new AddUserMembership(userId, groupId, roleId, assignedBy,
                    identityService);
            createUserMembership.execute();
            final SUserMembership sUserMembership = createUserMembership.getResult();
            return ModelConvertor.toUserMembership(sUserMembership);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void addUserMemberships(final List<Long> userIds, final long groupId, final long roleId)
            throws AlreadyExistsException, CreationException {
        // FIXME rewrite
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final long currentUserId = SessionInfos.getUserIdFromSession();
        try {
            final AddUserMemberships transactionContent = new AddUserMemberships(groupId, roleId, userIds,
                    identityService, currentUserId);
            transactionContent.execute();
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public UserMembership updateUserMembership(final long userMembershipId, final long newGroupId, final long newRoleId)
            throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserMembershipUpdateBuilderFactory.class)
                .createNewInstance()
                .updateGroupId(newGroupId).updateRoleId(newRoleId).done();
        try {
            final TransactionContent transactionContent = new UpdateMembershipByRoleIdAndGroupId(userMembershipId,
                    identityService, changeDescriptor);
            transactionContent.execute();
            final GetUserMembership getMembershipAfterUpdate = new GetUserMembership(userMembershipId, identityService);
            getMembershipAfterUpdate.execute();
            final SUserMembership sMembershipAfterUpdate = getMembershipAfterUpdate.getResult();
            return ModelConvertor.toUserMembership(sMembershipAfterUpdate);
        } catch (final SBonitaException sbe) {
            throw new UpdateException(sbe);
        }
    }

    @Override
    public void deleteUserMembership(final long userMembershipId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            identityService.deleteUserMembership(userMembershipId);
        } catch (final SIdentityException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public void deleteUserMembership(final long userId, final long groupId, final long roleId)
            throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            identityService.deleteLightUserMembership(identityService.getLightUserMembership(userId, groupId, roleId));
        } catch (final SIdentityException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public void deleteUserMemberships(final List<Long> userIds, final long groupId, final long roleId)
            throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            for (final long userId : userIds) {
                final SUserMembership userMembership = identityService.getLightUserMembership(userId, groupId, roleId);
                identityService.deleteLightUserMembership(userMembership);
            }
        } catch (final SIdentityException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public UserMembership getUserMembership(final long userMembershipId) throws MembershipNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetUserMembership getUserMembership = new GetUserMembership(userMembershipId, identityService);
        try {
            getUserMembership.execute();
            final SUserMembership sMembership = getUserMembership.getResult();
            return ModelConvertor.toUserMembership(sMembership);
        } catch (final SBonitaException sbe) {
            throw new MembershipNotFoundException(sbe);
        }
    }

    @Override
    public long getNumberOfUserMemberships(final long userId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfUserMemberships(userId,
                    identityService);
            transactionContent.execute();
            return transactionContent.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<UserMembership> getUserMemberships(final long userId, final int startIndex, final int maxResults,
            final UserMembershipCriterion pagingCrterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final IdentityService identityService = tenantAccessor.getIdentityService();
            OrderByOption orderByOption;
            switch (pagingCrterion) {
                case ROLE_NAME_DESC:
                    orderByOption = new OrderByOption(SRole.class, SRole.NAME, OrderByType.DESC);
                    break;
                case GROUP_NAME_ASC:
                    orderByOption = new OrderByOption(SGroup.class, SGroup.NAME, OrderByType.ASC);
                    break;
                case GROUP_NAME_DESC:
                    orderByOption = new OrderByOption(SGroup.class, SGroup.NAME, OrderByType.DESC);
                    break;
                // case ASSIGNED_BY_ASC:
                // orderByOption = new OrderByOption(SUserMembership.class, modelBuilder.getUserMembershipBuilder().getAssignedByKey(), OrderByType.ASC);
                // break;
                // case ASSIGNED_BY_DESC:
                // orderByOption = new OrderByOption(SUserMembership.class, modelBuilder.getUserMembershipBuilder().getAssignedByKey(), OrderByType.DESC);
                // break;
                case ASSIGNED_DATE_ASC:
                    orderByOption = new OrderByOption(SUserMembership.class, SUserMembership.ASSIGNED_DATE,
                            OrderByType.ASC);
                    break;
                case ASSIGNED_DATE_DESC:
                    orderByOption = new OrderByOption(SUserMembership.class, SUserMembership.ASSIGNED_DATE,
                            OrderByType.DESC);
                    break;
                case ROLE_NAME_ASC:
                default:
                    orderByOption = new OrderByOption(SRole.class, SRole.NAME, OrderByType.ASC);
                    break;
            }

            return getUserMemberships(userId, startIndex, maxResults, orderByOption, identityService);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    private List<UserMembership> getUserMemberships(final long userId, final int startIndex, final int maxResults,
            final OrderByOption orderByOption,
            final IdentityService identityService) throws SBonitaException {
        return getUserMemberships(userId, startIndex, maxResults, null, null, orderByOption, identityService);
    }

    private List<UserMembership> getUserMemberships(final long userId, final int startIndex, final int maxResults,
            final OrderByType orderExecutor,
            final String fieldExecutor, final OrderByOption orderByOption, final IdentityService identityService)
            throws SBonitaException {
        List<SUserMembership> sUserMemberships;
        if (userId == -1) {
            sUserMemberships = identityService.getUserMemberships(startIndex, maxResults, orderByOption);
        } else if (orderByOption != null) {
            sUserMemberships = identityService.getUserMembershipsOfUser(userId, startIndex, maxResults, orderByOption);
        } else if (fieldExecutor == null) {
            sUserMemberships = identityService.getUserMembershipsOfUser(userId, startIndex, maxResults);
        } else {
            sUserMemberships = identityService.getUserMembershipsOfUser(userId, startIndex, maxResults, fieldExecutor,
                    orderExecutor);
        }
        return ModelConvertor.toUserMembership(sUserMemberships);
    }

    @Override
    public List<UserMembership> getUserMembershipsByGroup(final long groupId, final int startIndex,
            final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetUserMembershipsOfGroup transactionContentWithResult = new GetUserMembershipsOfGroup(groupId,
                identityService, startIndex, maxResults);
        try {
            transactionContentWithResult.execute();
            return ModelConvertor.toUserMembership(transactionContentWithResult.getResult());
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<UserMembership> getUserMembershipsByRole(final long roleId, final int startIndex,
            final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final GetUserMembershipsOfRole transactionContentWithResult = new GetUserMembershipsOfRole(roleId,
                    identityService, startIndex, maxResults);
            transactionContentWithResult.execute();
            return ModelConvertor.toUserMembership(transactionContentWithResult.getResult());
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public void deleteOrganization() throws DeletionException {
        final OrganizationAPIImpl organizationAPIImpl = new OrganizationAPIImpl(getTenantAccessor(), 100);
        organizationAPIImpl.deleteOrganization();
    }

    @Override
    public void importOrganization(final String organizationContent) throws OrganizationImportException {
        importOrganization(organizationContent, ImportPolicy.MERGE_DUPLICATES);
    }

    @Override
    public void importOrganization(final String organizationContent, final ImportPolicy policy)
            throws OrganizationImportException {
        importOrganizationWithWarnings(organizationContent, policy);
    }

    @Override
    public List<String> importOrganizationWithWarnings(String organizationContent, ImportPolicy policy)
            throws OrganizationImportException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final SCustomUserInfoValueUpdateBuilderFactory updaterFactor = BuilderFactory
                    .get(SCustomUserInfoValueUpdateBuilderFactory.class);
            final SCustomUserInfoValueAPI customUserInfoValueAPI = new SCustomUserInfoValueAPI(
                    tenantAccessor.getIdentityService(),
                    updaterFactor);
            ImportOrganization importedOrganization = new ImportOrganization(tenantAccessor, organizationContent,
                    policy, customUserInfoValueAPI);
            return importedOrganization.execute();
        } catch (JAXBException e) {
            throw new InvalidOrganizationFileFormatException(e);
        } catch (final SBonitaException e) {
            throw new OrganizationImportException(e);
        }
    }

    @Override
    public String exportOrganization() throws OrganizationExportException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final int maxResults = 100;
        final ExportOrganization exportOrganization = new ExportOrganization(tenantAccessor.getIdentityService(),
                maxResults);
        try {
            exportOrganization.execute();
            return exportOrganization.getResult();
        } catch (final SBonitaException e) {
            throw new OrganizationExportException(e);
        }
    }

    @Override
    public CustomUserInfoDefinition createCustomUserInfoDefinition(final CustomUserInfoDefinitionCreator creator)
            throws CreationException,
            AlreadyExistsException {
        return createCustomUserInfoDefinitionAPI().create(creator);
    }

    @Override
    public List<CustomUserInfoDefinition> getCustomUserInfoDefinitions(final int startIndex, final int maxResult)
            throws RetrieveException {
        return createCustomUserInfoDefinitionAPI().list(startIndex, maxResult);
    }

    @Override
    public long getNumberOfCustomInfoDefinitions() {
        return createCustomUserInfoDefinitionAPI().count();
    }

    @Override
    public void deleteCustomUserInfoDefinition(final long id) throws DeletionException {
        createCustomUserInfoDefinitionAPI().delete(id);
    }

    @Override
    public List<CustomUserInfo> getCustomUserInfo(final long userId, final int startIndex, final int maxResult) {
        try {
            return createCustomUserInfoAPI().list(userId, startIndex, maxResult);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<CustomUserInfoValue> searchCustomUserInfoValues(final SearchOptions options) {
        try {
            return createCustomUserInfoValueAPI().search(
                    getTenantAccessor().getSearchEntitiesDescriptor().getSearchCustomUserInfoValueDescriptor(),
                    options);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public CustomUserInfoValue setCustomUserInfoValue(final long definitionId, final long userId, final String value)
            throws UpdateException {
        try {
            return ModelConvertor.convert(createCustomUserInfoValueAPI().set(definitionId, userId, value));
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public Icon getIcon(long id) throws NotFoundException {
        try {
            SIcon icon = getTenantAccessor().getIconService().getIcon(id);
            if (icon == null) {
                throw new NotFoundException("unable to find icon with id " + id);
            }
            return ModelConvertor.toIcon(icon);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    private Sort getSortFromCriterion(UserCriterion criterion) {
        Sort sort = new Sort();
        switch (criterion) {
            case FIRST_NAME_ASC:
                sort.setField(SUser.FIRST_NAME);
                sort.setOrder(OrderByType.ASC);
                break;
            case LAST_NAME_ASC:
                sort.setField(SUser.LAST_NAME);
                sort.setOrder(OrderByType.ASC);
                break;
            case USER_NAME_ASC:
                sort.setField(SUser.USER_NAME);
                sort.setOrder(OrderByType.ASC);
                break;
            case FIRST_NAME_DESC:
                sort.setField(SUser.FIRST_NAME);
                sort.setOrder(OrderByType.DESC);
                break;
            case LAST_NAME_DESC:
                sort.setField(SUser.LAST_NAME);
                sort.setOrder(OrderByType.DESC);
                break;
            case USER_NAME_DESC:
                sort.setField(SUser.USER_NAME);
                sort.setOrder(OrderByType.DESC);
                break;
            default:
                throw new IllegalStateException();
        }
        return sort;
    }

    private CustomUserInfoAPIDelegate createCustomUserInfoAPI() {
        return new CustomUserInfoAPIDelegate(getTenantAccessor().getIdentityService());
    }

    private CustomUserInfoDefinitionAPIDelegate createCustomUserInfoDefinitionAPI() {
        return new CustomUserInfoDefinitionAPIDelegate(getTenantAccessor().getIdentityService());
    }

    private SCustomUserInfoValueAPI createCustomUserInfoValueAPI() {
        return new SCustomUserInfoValueAPI(getTenantAccessor().getIdentityService(),
                BuilderFactory.get(SCustomUserInfoValueUpdateBuilderFactory.class));
    }

    private class Sort {

        private String field = null;
        private OrderByType order = null;

        private Sort() {

        }

        private Sort(String field, OrderByType order) {
            this.field = field;
            this.order = order;
        }

        private String getField() {
            return field;
        }

        private void setField(String field) {
            this.field = field;
        }

        private OrderByType getOrder() {
            return order;
        }

        private void setOrder(OrderByType order) {
            this.order = order;
        }
    }

}
