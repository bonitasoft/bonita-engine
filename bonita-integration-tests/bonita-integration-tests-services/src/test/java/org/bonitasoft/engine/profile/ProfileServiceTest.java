package org.bonitasoft.engine.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserCreationException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ProfileServiceTest extends CommonServiceTest {

    private static ProfileService profileService;

    private static IdentityService identityService;

    static {
        profileService = getServicesBuilder().buildProfileService();
        identityService = getServicesBuilder().buildIdentityService();
    }

    @Test(expected = SProfileNotFoundException.class)
    public void cannotGetAnUnknownProfile() throws SBonitaException {
        try {
            getTransactionService().begin();
            profileService.getProfile(10);
            Assert.fail();
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void getProfile() throws SBonitaException {
        getTransactionService().begin();
        final SProfile profile = BuilderFactory.get(SProfileBuilderFactory.class).createNewInstance("profile1", true, 0, 0, 0, 0).done();
        final SProfile createdProfile = profileService.createProfile(profile);
        final SProfile gotProfile = profileService.getProfile(createdProfile.getId());
        Assert.assertEquals(createdProfile, gotProfile);
        profileService.deleteProfile(gotProfile);
        try {
            profileService.getProfile(createdProfile.getId());
            Assert.fail();
        } catch (final SProfileNotFoundException spnfe) {
            getTransactionService().complete();
        }
    }

    @Test
    public void getUserProfile() throws SBonitaException {
        getTransactionService().begin();
        final SProfile profile = profileService.createProfile(BuilderFactory.get(SProfileBuilderFactory.class).createNewInstance("profile1", true, 0, 0, 0, 0)
                .done());

        final List<OrderByOption> orderByOptions = getOrderByOptions();
        final QueryOptions queryOptions = new QueryOptions(0, 10, orderByOptions, Collections.singletonList(new FilterOption(SProfileMember.class, "profileId",
                profile.getId())), null);

        List<SProfileMember> profileMembers = profileService.searchProfileMembers("ForUser", queryOptions);
        Assert.assertEquals(0, profileMembers.size());

        SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("john").setPassword("bpm")
                .setFirstName("John").setLastName("Doe");
        final SUser john = identityService.createUser(userBuilder.done());

        userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("jane").setPassword("bpm").setFirstName("Jane")
                .setLastName("Doe");
        final SUser jane = identityService.createUser(userBuilder.done());

        final SProfileMember johnProfileMember = profileService.addUserToProfile(profile.getId(), john.getId(), "John", "Doe", "john");
        final SProfileMember janeProfileMember = profileService.addUserToProfile(profile.getId(), jane.getId(), "Jane", "Doe", "jane");
        profileMembers = profileService.searchProfileMembers("ForUser", queryOptions);
        Assert.assertEquals(2, profileMembers.size());

        profileService.deleteProfileMember(johnProfileMember);
        profileService.deleteProfileMember(janeProfileMember);
        profileMembers = profileService.searchProfileMembers("ForUser", queryOptions);
        Assert.assertEquals(0, profileMembers.size());

        identityService.deleteUser(john);
        identityService.deleteUser(jane);
        profileService.deleteProfile(profile);
        getTransactionService().complete();
    }

    private List<OrderByOption> getOrderByOptions() {
        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(1);
        orderByOptions.add(new OrderByOption(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getFirstNameKey(), OrderByType.ASC));
        return orderByOptions;
    }

    private SUser createUser(final String username, final String password) throws SUserCreationException {
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(username).setPassword(password);
        return identityService.createUser(userBuilder.done());
    }

    @Test
    public void getProfileOfUserFrom() throws SBonitaException {
        getTransactionService().begin();
        final SProfile profile = profileService.createProfile(BuilderFactory.get(SProfileBuilderFactory.class).createNewInstance("profile1", false, 0, 0, 0, 0)
                .done());

        final SUser john = createUser("john", "bpm");
        final SUser jane = createUser("jane", "bpm");

        List<SProfile> profilesOfUser = profileService.searchProfilesOfUser(john.getId(), 0, 10, "name", OrderByType.ASC);
        Assert.assertEquals(0, profilesOfUser.size());

        final SProfileMember johnProfileMember = profileService.addUserToProfile(profile.getId(), john.getId(), "John", "Doe", "john");
        final SProfileMember janeProfileMember = profileService.addUserToProfile(profile.getId(), jane.getId(), "Jane", "Doe", "jane");

        profilesOfUser = profileService.searchProfilesOfUser(john.getId(), 0, 10, "name", OrderByType.ASC);
        Assert.assertEquals(1, profilesOfUser.size());

        final QueryOptions countOptions = new QueryOptions(0, 10, null, Collections.singletonList(new FilterOption(SProfileMember.class, "profileId", profile
                .getId())), null);

        Assert.assertEquals(2, profileService.getNumberOfProfileMembers("ForUser", countOptions));

        profileService.deleteProfileMember(johnProfileMember);
        profileService.deleteProfileMember(janeProfileMember);

        profilesOfUser = profileService.searchProfilesOfUser(john.getId(), 0, 10, "name", OrderByType.ASC);
        Assert.assertEquals(0, profilesOfUser.size());

        identityService.deleteUser(john);
        identityService.deleteUser(jane);
        profileService.deleteProfile(profile);
        getTransactionService().complete();
    }

}
