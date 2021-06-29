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
package org.bonitasoft.engine.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ApplicationBuilder.anApplication;
import static org.bonitasoft.engine.test.persistence.builder.ApplicationMenuBuilder.anApplicationMenu;
import static org.bonitasoft.engine.test.persistence.builder.ApplicationPageBuilder.anApplicationPage;
import static org.bonitasoft.engine.test.persistence.builder.GroupBuilder.aGroup;
import static org.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;
import static org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder.DEFAULT_TENANT_ID;
import static org.bonitasoft.engine.test.persistence.builder.ProfileBuilder.aProfile;
import static org.bonitasoft.engine.test.persistence.builder.ProfileMemberBuilder.aProfileMember;
import static org.bonitasoft.engine.test.persistence.builder.RoleBuilder.aRole;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.page.AbstractSPage;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.test.persistence.repository.ApplicationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ApplicationQueriesTest {

    @Inject
    private ApplicationRepository repository;

    @Test
    public void getApplicationByToken_returns_the_application_with_the_given_token() {
        //given
        repository.add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                .withVersion("1.0").withPath("app1").build());
        final AbstractSApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2")
                        .build());
        repository.add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                .withVersion("1.0").withPath("app3").build());

        //when
        final SApplication retrievedApp = repository.getApplicationByToken("app2");

        //then
        assertThat(retrievedApp)
                .isEqualTo(repository.getById(SApplication.class, application2.getId(), DEFAULT_TENANT_ID));
    }

    @Test
    public void getApplication_returns_the_application_with_the_given_id() {
        //given
        repository.add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                .withVersion("1.0").withPath("app1").build());
        final AbstractSApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withVersion("1.0").withPath("app1").build());
        repository.add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                .withVersion("1.0").withPath("app1").build());

        //when
        final SApplicationWithIcon retrievedApp = repository.getById(SApplicationWithIcon.class, application2.getId(),
                DEFAULT_TENANT_ID);

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplicationPageById_should_return_the_applicationPage_identified_by_the_given_id() {
        //given
        final AbstractSApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1").build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        final SApplicationPage secondPageApp = repository
                .add(anApplicationPage().withToken("SecondPage").withApplicationId(application1.getId())
                        .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("ThirdPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPage(secondPageApp.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp);
    }

    @Test
    public void getApplicationPageByNameAnApplicationName_should_return_the_applicationPage_with_the_given_name_in_the_given_application() {
        //given
        final AbstractSApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final AbstractSApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app2")
                        .build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        final SApplicationPage secondPageApp1 = repository
                .add(anApplicationPage().withToken("SecondPage").withApplicationId(application1.getId())
                        .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application2.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application2.getId())
                .withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPageByTokenAndApplicationToken("app1",
                "SecondPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp1);
    }

    @Test
    public void getApplicationPageByTokenAndApplicationId_should_return_the_applicationPage_with_the_given_name_in_the_given_application() {
        //given
        final AbstractSApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final AbstractSApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app2")
                        .build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        final SApplicationPage secondPageApp1 = repository
                .add(anApplicationPage().withToken("SecondPage").withApplicationId(application1.getId())
                        .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application2.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application2.getId())
                .withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository
                .getApplicationPageByTokenAndApplicationId(application1.getId(), "SecondPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp1);
    }

    @Test
    public void getAllPagesForProfile_should_return_layouts_and_all_pages_related_to_application() {
        //given
        //used by app1 and app2
        SProfile firstProfile = repository.add(aProfile().withName("firstProfile").build());

        //used by app3
        SProfile secondProfile = repository.add(aProfile().withName("secondProfile").build());

        //not used
        SProfile thirdProfile = repository.add(aProfile().withName("thirdProfile").build());

        AbstractSPage layoutApp1 = repository
                .add(aPage().withName("layoutApp1").withContent("The content".getBytes()).build());
        AbstractSPage themeApp2 = repository
                .add(aPage().withName("themeApp2").withContent("The content".getBytes()).build());
        AbstractSPage layoutApp4 = repository
                .add(aPage().withName("layoutApp4").withContent("The content".getBytes()).build());
        AbstractSPage themeApp4 = repository
                .add(aPage().withName("themeApp4").withContent("The content".getBytes()).build());
        final AbstractSApplication application1 = repository.add(anApplication().withToken("app1")
                .withDisplayName("my app1")
                .withDisplayName("my app1")
                .withVersion("1.0").withPath("/app1").withProfile(firstProfile.getId()).withLayout(layoutApp1.getId())
                .build());
        final AbstractSApplication application2 = repository.add(anApplication().withToken("app2")
                .withDisplayName("my app2")
                .withDisplayName("my app2")
                .withVersion("1.0").withPath("/app2").withProfile(firstProfile.getId()).withTheme(themeApp2.getId())
                .build());
        final AbstractSApplication application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("/app3").withProfile(secondProfile.getId())
                        .build());
        final AbstractSApplication application4 = repository.add(anApplication().withToken("app4")
                .withDisplayName("my app4")
                .withDisplayName("my app4")
                .withVersion("1.0").withPath("/app4").withLayout(layoutApp4.getId()).withTheme(themeApp4.getId())
                .build());

        final AbstractSPage page1 = repository
                .add(aPage().withName("page1").withContent("The content".getBytes()).build());
        final AbstractSPage page2 = repository
                .add(aPage().withName("page2").withContent("The content".getBytes()).build());
        final AbstractSPage page3 = repository
                .add(aPage().withName("page3").withContent("The content".getBytes()).build());
        final AbstractSPage page4 = repository
                .add(aPage().withName("page4").withContent("The content".getBytes()).build());
        final AbstractSPage page5 = repository
                .add(aPage().withName("page5").withContent("The content".getBytes()).build());
        final AbstractSPage page6 = repository
                .add(aPage().withName("page6").withContent("The content".getBytes()).build());

        //app1 has layout layoutApp1 and references page1 and page2
        repository.add(anApplicationPage().withToken("FirstPageApp1").withApplicationId(application1.getId())
                .withPageId(page1.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPageApp1").withApplicationId(application1.getId())
                .withPageId(page1.getId()).build());
        repository.add(anApplicationPage().withToken("ThirdPageApp1").withApplicationId(application1.getId())
                .withPageId(page2.getId()).build());

        //app2 has layout themeApp2 and references page3 and page4
        repository.add(anApplicationPage().withToken("FirstPageApp2").withApplicationId(application2.getId())
                .withPageId(page3.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPageApp2").withApplicationId(application2.getId())
                .withPageId(page4.getId()).build());

        //app3 has no layout and references page4 and page5
        repository.add(anApplicationPage().withToken("FirstPageApp3").withApplicationId(application3.getId())
                .withPageId(page4.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPageApp3").withApplicationId(application3.getId())
                .withPageId(page5.getId()).build());

        //app3 has layout layoutApp4, themeApp4 and references page6
        repository.add(anApplicationPage().withToken("FirstPageApp4").withApplicationId(application4.getId())
                .withPageId(page6.getId()).build());

        //when
        List<String> pagesForProfile = repository.getAllPagesForProfile(firstProfile.getId());

        //then
        assertThat(pagesForProfile).containsExactlyInAnyOrder("layoutApp1", "page1", "page2", "page3", "page4",
                "themeApp2");

        //when
        pagesForProfile = repository.getAllPagesForProfile(secondProfile.getId());

        //then
        assertThat(pagesForProfile).containsExactlyInAnyOrder("page4", "page5");

        //when
        pagesForProfile = repository.getAllPagesForProfile(thirdProfile.getId());

        //then
        assertThat(pagesForProfile).isEmpty();
    }

    @Test
    public void getApplicationHomePage_should_return_the_applicationPage_set_as_home_page_for_the_given_application() {
        //given
        final AbstractSApplication application = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage firstPage = repository
                .add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                        .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());

        application.setHomePageId(firstPage.getId());
        repository.update(application);

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationHomePage(application.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(firstPage);
    }

    @Test
    public void getApplicationMenu_by_id_should_return_the_application_menu_identified_by_the_given_id() {
        //given
        final AbstractSApplication application = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage appPage = repository
                .add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                        .withPageId(page.getId()).build());
        final SApplicationMenu menu = repository
                .add(anApplicationMenu().withApplicationId(application.getId()).withApplicationPageId(appPage.getId())
                        .withDisplayName("menu app1").withIndex(1)
                        .build());

        //when
        final SApplicationMenu retrievedMenu = repository.getApplicationMenu(menu.getId());

        //then
        assertThat(retrievedMenu).isEqualTo(retrievedMenu);
    }

    @Test
    public void getLastIndexForRootMenu_should_return_last_used_index() {
        //given
        final AbstractSApplication application = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage appPage = repository
                .add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                        .withPageId(page.getId()).build());
        repository.add(anApplicationMenu().withApplicationId(application.getId()).withApplicationPageId(appPage.getId())
                .withDisplayName("menu app1")
                .withIndex(1)
                .build());

        //when
        final int lastIndex = repository.getLastIndexForRootMenu();

        //then
        assertThat(lastIndex).isEqualTo(1);
    }

    @Test
    public void getLastIndexForChildMenu_should_return_last_used_index_by_children_of_a_given_parent() {
        //given
        final AbstractSApplication application = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage appPage = repository
                .add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                        .withPageId(page.getId()).build());
        final SApplicationMenu parentMenu = repository
                .add(anApplicationMenu().withApplicationId(application.getId()).withApplicationPageId(appPage.getId())
                        .withDisplayName("menu app1").withIndex(1)
                        .build());
        repository.add(anApplicationMenu().withApplicationId(application.getId()).withParentId(parentMenu.getId())
                .withApplicationPageId(appPage.getId())
                .withDisplayName("menu app1").withIndex(1)
                .build());

        //when
        final int lastIndex = repository.getLastIndexForChildOf(parentMenu.getId());

        //then
        assertThat(lastIndex).isEqualTo(1);
    }

    @Test
    public void getApplicationOfUser_returns_the_application_for_the_given_user() {
        //given
        SUser user1 = repository.add(aUser().withId(1L).withUserName("walter.bates").build());
        SUser user2 = repository.add(aUser().withId(2L).withUserName("helen.kelly").build());
        SUser user3 = repository.add(aUser().withId(3L).withUserName("daniela.angelo").build());
        SUser user4 = repository.add(aUser().withId(4L).withUserName("jan.fisher").build());

        final SProfile profile1 = repository.add(aProfile().withName("firstProfile").build());
        repository.add(aProfileMember().withUserId(user1.getId()).withProfileId(profile1.getId()).build());
        repository.add(aProfileMember().withUserId(user3.getId()).withProfileId(profile1.getId()).build());

        final SProfile profile2 = repository.add(aProfile().withName("secondProfile").build());
        repository.add(aProfileMember().withUserId(user2.getId()).withProfileId(profile2.getId()).build());
        repository.add(aProfileMember().withUserId(user3.getId()).withProfileId(profile2.getId()).build());

        long application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build())
                .getId();
        long application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2").withProfile(profile1.getId())
                        .build())
                .getId();
        long application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("app3").withProfile(profile2.getId()).build())
                .getId();

        repository.flush();

        //then
        assertThat(repository.getNumberOfApplicationOfUser(user1.getId())).isEqualTo(2);
        assertThat(repository.getNumberOfApplicationOfUser(user2.getId())).isEqualTo(1);
        assertThat(repository.getNumberOfApplicationOfUser(user3.getId())).isEqualTo(3);
        assertThat(repository.getNumberOfApplicationOfUser(user4.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(5L)).isEqualTo(0);
        assertThat(repository.searchApplicationOfUser(user1.getId())).extracting("id").containsExactlyInAnyOrder(
                application1,
                application2);
        assertThat(repository.searchApplicationOfUser(user2.getId())).extracting("id").containsExactly(application3);
        assertThat(repository.searchApplicationOfUser(user3.getId())).extracting("id").containsExactlyInAnyOrder(
                application1,
                application2,
                application3);
        assertThat(repository.searchApplicationOfUser(user4.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(5L)).isEmpty();

    }

    @Test
    public void getApplicationOfUser_returns_the_application_only_once_even_if_mapped_twice() {
        //given
        SUser user = repository.add(aUser().withId(1L).withUserName("walter.bates").build());
        SGroup group = repository.add(aGroup().forGroupId(20L).forGroupName("Group1").build());
        repository.add(aUserMembership().forUser(user.getId()).memberOf(group.getId(), -1L).build());
        final SProfile profile1 = repository.add(aProfile().withName("firstProfile").build());
        repository.add(aProfileMember().withUserId(user.getId()).withProfileId(profile1.getId()).build());

        repository.add(aProfileMember().withGroupId(group.getId()).withProfileId(profile1.getId()).build());

        long applicationId = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build())
                .getId();

        repository.flush();

        //then
        assertThat(repository.getNumberOfApplicationOfUser(user.getId())).isEqualTo(1);
        final List<SApplication> applicationList = repository.searchApplicationOfUser(user.getId());
        assertThat(applicationList).hasSize(1);
        assertThat(applicationList.get(0).getId()).isEqualTo(applicationId);
    }

    @Test
    public void searchApplicationOfUser_returns_the_application_for_the_given_user_mapped_through_group() {
        //given
        SUser user1 = repository.add(aUser().withId(1L).withUserName("walter.bates").build());
        SUser user2 = repository.add(aUser().withId(2L).withUserName("helen.kelly").build());
        SUser user3 = repository.add(aUser().withId(3L).withUserName("daniela.angelo").build());
        SUser user4 = repository.add(aUser().withId(4L).withUserName("jan.fisher").build());
        SUser user5 = repository.add(aUser().withId(5L).withUserName("antonio.banderas").build());
        SGroup group1 = repository.add(aGroup().forGroupId(20L).forGroupName("Group1").build());
        SGroup group2 = repository
                .add(aGroup().forGroupId(21L).forGroupName("Group2").forParentPath("/Group1").build());
        SGroup group3 = repository.add(aGroup().forGroupId(22L).forGroupName("Group3").forParentPath("").build());

        final SProfile profile1 = repository.add(aProfile().withName("firstProfile").build());
        repository.add(aProfileMember().withGroupId(group1.getId()).withProfileId(profile1.getId()).build());
        repository.add(aUserMembership().forUser(user1.getId()).memberOf(group1.getId(), -1L).build());
        repository.add(aUserMembership().forUser(user3.getId()).memberOf(group1.getId(), -1L).build());

        final SProfile profile2 = repository.add(aProfile().withName("secondProfile").build());
        repository.add(aProfileMember().withGroupId(group2.getId()).withProfileId(profile2.getId()).build());
        repository.add(aUserMembership().forUser(user2.getId()).memberOf(group2.getId(), -1L).build());
        repository.add(aUserMembership().forUser(user3.getId()).memberOf(group2.getId(), -1L).build());

        repository.add(aUserMembership().forUser(user4.getId()).memberOf(group3.getId(), -1L).build());

        final AbstractSApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build());
        final AbstractSApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2").withProfile(profile1.getId())
                        .build());
        final AbstractSApplication application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("app3").withProfile(profile2.getId()).build());

        repository.flush();

        //then
        assertThat(repository.searchApplicationOfUser(user1.getId())).extracting("id")
                .containsExactlyInAnyOrder(application1.getId(), application2.getId());
        assertThat(repository.searchApplicationOfUser(user2.getId())).extracting("id")
                .containsExactly(application3.getId());
        assertThat(repository.searchApplicationOfUser(user3.getId())).extracting("id")
                .containsExactlyInAnyOrder(application1.getId(), application2.getId(), application3.getId());
        assertThat(repository.searchApplicationOfUser(user4.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(user5.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(6L)).isEmpty();
        assertThat(repository.getNumberOfApplicationOfUser(user1.getId())).isEqualTo(2);
        assertThat(repository.getNumberOfApplicationOfUser(user2.getId())).isEqualTo(1);
        assertThat(repository.getNumberOfApplicationOfUser(user3.getId())).isEqualTo(3);
        assertThat(repository.getNumberOfApplicationOfUser(user4.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(user5.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(6L)).isEqualTo(0);
    }

    @Test
    public void getApplicationOfUser_returns_the_application_for_the_given_user_mapped_through_role() {
        //given
        SUser user1 = repository.add(aUser().withId(1L).withUserName("walter.bates").build());
        SUser user2 = repository.add(aUser().withId(2L).withUserName("helen.kelly").build());
        SUser user3 = repository.add(aUser().withId(3L).withUserName("daniela.angelo").build());
        SUser user4 = repository.add(aUser().withId(4L).withUserName("jan.fisher").build());
        SUser user5 = repository.add(aUser().withId(5L).withUserName("antonio.banderas").build());
        SRole role1 = repository.add(aRole().forRoleId(40L).forRoleName("Role1").build());
        SRole role2 = repository.add(aRole().forRoleId(41L).forRoleName("Role2").build());
        SRole role3 = repository.add(aRole().forRoleId(42L).forRoleName("Role3").build());

        final SProfile profile1 = repository.add(aProfile().withName("firstProfile").build());
        repository.add(aProfileMember().withRoleId(role1.getId()).withProfileId(profile1.getId()).build());
        repository.add(aUserMembership().forUser(user1.getId()).memberOf(-1L, role1.getId()).build());
        repository.add(aUserMembership().forUser(user3.getId()).memberOf(-1L, role1.getId()).build());

        final SProfile profile2 = repository.add(aProfile().withName("secondProfile").build());
        repository.add(aProfileMember().withRoleId(role2.getId()).withProfileId(profile2.getId()).build());
        repository.add(aUserMembership().forUser(user2.getId()).memberOf(-1L, role2.getId()).build());
        repository.add(aUserMembership().forUser(user3.getId()).memberOf(-1L, role2.getId()).build());

        repository.add(aUserMembership().forUser(user4.getId()).memberOf(-1L, role3.getId()).build());

        long application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build())
                .getId();
        long application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2").withProfile(profile1.getId())
                        .build())
                .getId();
        long application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("app3").withProfile(profile2.getId()).build())
                .getId();

        repository.flush();

        //then
        assertThat(repository.searchApplicationOfUser(user1.getId())).extracting("id").containsExactlyInAnyOrder(
                application1,
                application2);
        assertThat(repository.searchApplicationOfUser(user2.getId())).extracting("id").containsExactly(application3);
        assertThat(repository.searchApplicationOfUser(user3.getId())).extracting("id").containsExactlyInAnyOrder(
                application1,
                application2,
                application3);
        assertThat(repository.searchApplicationOfUser(user4.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(user5.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(6L)).isEmpty();

        //then
        assertThat(repository.getNumberOfApplicationOfUser(user1.getId())).isEqualTo(2);
        assertThat(repository.getNumberOfApplicationOfUser(user2.getId())).isEqualTo(1);
        assertThat(repository.getNumberOfApplicationOfUser(user3.getId())).isEqualTo(3);
        assertThat(repository.getNumberOfApplicationOfUser(user4.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(user5.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(6L)).isEqualTo(0);
    }

    @Test
    public void getApplicationOfUser_returns_the_application_for_the_given_user_mapped_through_group_and_role() {
        //given
        SUser user1 = repository.add(aUser().withId(1L).withUserName("walter.bates").build());
        SUser user2 = repository.add(aUser().withId(2L).withUserName("helen.kelly").build());
        SUser user3 = repository.add(aUser().withId(3L).withUserName("daniela.angelo").build());
        SUser user4 = repository.add(aUser().withId(4L).withUserName("jan.fisher").build());
        SUser user5 = repository.add(aUser().withId(5L).withUserName("antonio.banderas").build());
        SGroup group1 = repository.add(aGroup().forGroupId(20L).forGroupName("Group1").build());
        SGroup group2 = repository
                .add(aGroup().forGroupId(21L).forGroupName("Group2").forParentPath("/Group1").build());
        SGroup group3 = repository.add(aGroup().forGroupId(22L).forGroupName("Group3").forParentPath("").build());
        SRole role1 = repository.add(aRole().forRoleId(40L).forRoleName("Role1").build());
        SRole role2 = repository.add(aRole().forRoleId(41L).forRoleName("Role2").build());
        SRole role3 = repository.add(aRole().forRoleId(42L).forRoleName("Role3").build());

        final SProfile profile1 = repository.add(aProfile().withName("firstProfile").build());
        repository.add(aProfileMember().withGroupId(group1.getId()).withRoleId(role1.getId())
                .withProfileId(profile1.getId()).build());
        repository.add(aUserMembership().forUser(user1.getId()).memberOf(group1.getId(), role1.getId()).build());
        repository.add(aUserMembership().forUser(user3.getId()).memberOf(group1.getId(), role1.getId()).build());

        final SProfile profile2 = repository.add(aProfile().withName("secondProfile").build());
        repository.add(aProfileMember().withGroupId(group2.getId()).withRoleId(role2.getId())
                .withProfileId(profile2.getId()).build());
        repository.add(aUserMembership().forUser(user2.getId()).memberOf(group2.getId(), role2.getId()).build());
        repository.add(aUserMembership().forUser(user3.getId()).memberOf(group2.getId(), role2.getId()).build());

        repository.add(aUserMembership().forUser(user4.getId()).memberOf(group3.getId(), role3.getId()).build());

        long application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build())
                .getId();
        long application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2").withProfile(profile1.getId())
                        .build())
                .getId();
        long application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("app3").withProfile(profile2.getId()).build())
                .getId();

        repository.flush();

        //then
        assertThat(repository.searchApplicationOfUser(user1.getId())).extracting("id").containsExactlyInAnyOrder(
                application1,
                application2);
        assertThat(repository.searchApplicationOfUser(user2.getId())).extracting("id").containsExactly(application3);
        assertThat(repository.searchApplicationOfUser(user3.getId())).extracting("id").containsExactlyInAnyOrder(
                application1,
                application2,
                application3);
        assertThat(repository.searchApplicationOfUser(user4.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(user5.getId())).isEmpty();
        assertThat(repository.searchApplicationOfUser(6L)).isEmpty();

        //then
        assertThat(repository.getNumberOfApplicationOfUser(user1.getId())).isEqualTo(2);
        assertThat(repository.getNumberOfApplicationOfUser(user2.getId())).isEqualTo(1);
        assertThat(repository.getNumberOfApplicationOfUser(user3.getId())).isEqualTo(3);
        assertThat(repository.getNumberOfApplicationOfUser(user4.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(user5.getId())).isEqualTo(0);
        assertThat(repository.getNumberOfApplicationOfUser(6L)).isEqualTo(0);
    }

}
