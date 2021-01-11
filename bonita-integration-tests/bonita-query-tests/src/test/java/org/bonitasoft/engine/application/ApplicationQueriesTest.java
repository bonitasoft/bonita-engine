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
import static org.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;
import static org.bonitasoft.engine.test.persistence.builder.ProfileBuilder.aProfile;
import static org.bonitasoft.engine.test.persistence.builder.ProfileMemberBuilder.aProfileMember;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
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
    public void getApplicationByToken_returns_the_application_with_the_given_token() throws Exception {
        //given
        repository.add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                .withVersion("1.0").withPath("app1").build());
        final SApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2")
                        .build());
        repository.add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                .withVersion("1.0").withPath("app3").build());

        //when
        final SApplication retrievedApp = repository.getApplicationByToken("app2");

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplication_returns_the_application_with_the_given_id() throws Exception {
        //given
        repository.add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                .withVersion("1.0").withPath("app1").build());
        final SApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withVersion("1.0").withPath("app1").build());
        repository.add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                .withVersion("1.0").withPath("app1").build());

        //when
        final SApplication retrievedApp = repository.getApplication(application2.getId());

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplicationPageById_should_return_the_applicationPage_identified_by_the_given_id() throws Exception {
        //given
        final SApplication application1 = repository
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
    public void getApplicationPageByNameAnApplicationName_should_return_the_applicationPage_with_the_given_name_in_the_given_application()
            throws Exception {
        //given
        final SApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final SApplication application2 = repository
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
    public void getApplicationPageByTokenAndApplicationId_should_return_the_applicationPage_with_the_given_name_in_the_given_application()
            throws Exception {
        //given
        final SApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app")
                        .withVersion("1.0").withPath("/app1")
                        .build());
        final SApplication application2 = repository
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
    public void getAllPagesForProfile_should_return_layouts_and_all_pages_related_to_application() throws Exception {
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
        final SApplication application1 = repository.add(anApplication().withToken("app1").withDisplayName("my app1")
                .withDisplayName("my app1")
                .withVersion("1.0").withPath("/app1").withProfile(firstProfile.getId()).withLayout(layoutApp1.getId())
                .build());
        final SApplication application2 = repository.add(anApplication().withToken("app2").withDisplayName("my app2")
                .withDisplayName("my app2")
                .withVersion("1.0").withPath("/app2").withProfile(firstProfile.getId()).withTheme(themeApp2.getId())
                .build());
        final SApplication application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("/app3").withProfile(secondProfile.getId())
                        .build());
        final SApplication application4 = repository.add(anApplication().withToken("app4").withDisplayName("my app4")
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
        assertThat(pagesForProfile).containsExactly("layoutApp1", "page1", "page2", "page3", "page4", "themeApp2");

        //when
        pagesForProfile = repository.getAllPagesForProfile(secondProfile.getId());

        //then
        assertThat(pagesForProfile).containsExactly("page4", "page5");

        //when
        pagesForProfile = repository.getAllPagesForProfile(thirdProfile.getId());

        //then
        assertThat(pagesForProfile).isEmpty();
    }

    @Test
    public void getApplicationHomePage_should_return_the_applicationPage_set_as_home_page_for_the_given_application()
            throws Exception {
        //given
        final SApplication application = repository
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
    public void getApplicationMenu_by_id_should_return_the_application_menu_identified_by_the_given_id()
            throws Exception {
        //given
        final SApplication application = repository
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
    public void getLastIndexForRootMenu_should_return_last_used_index() throws Exception {
        //given
        final SApplication application = repository
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
    public void getLastIndexForChildMenu_should_return_last_used_index_by_children_of_a_given_parent()
            throws Exception {
        //given
        final SApplication application = repository
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
    public void getNumberOfApplicationOfUser_returns_the_application_for_the_given_user() throws Exception {
        //given
        SUser user1 = aUser().withId(1L).withUserName("walter.bates").build();
        repository.add(user1);
        SUser user2 = aUser().withId(2L).withUserName("helen.kelly").build();
        repository.add(user2);
        SUser user3 = aUser().withId(3L).withUserName("daniela.angelo").build();
        repository.add(user3);
        SUser user4 = aUser().withId(4L).withUserName("jan.fisher").build();
        repository.add(user4);

        final String profileName1 = "firstProfile";
        final SProfile profile1 = aProfile().withName(profileName1).build();
        repository.add(profile1);
        repository.add(aProfileMember().withUserId(user1.getId()).withProfileId(profile1.getId()).build());
        repository.add(aProfileMember().withUserId(user3.getId()).withProfileId(profile1.getId()).build());

        final String profileName2 = "secondProfile";
        final SProfile profile2 = aProfile().withName(profileName2).build();
        repository.add(profile2);
        repository.add(aProfileMember().withUserId(user2.getId()).withProfileId(profile2.getId()).build());
        repository.add(aProfileMember().withUserId(user3.getId()).withProfileId(profile2.getId()).build());

        final SApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build());
        final SApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2").withProfile(profile1.getId())
                        .build());
        final SApplication application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("app3").withProfile(profile2.getId()).build());

        repository.flush();

        //when

        final Long numberOfApplicationsForUser1 = repository.getNumberOfApplicationOfUser(user1.getId());
        final Long numberOfApplicationsForUser2 = repository.getNumberOfApplicationOfUser(user2.getId());
        final Long numberOfApplicationsForUser3 = repository.getNumberOfApplicationOfUser(user3.getId());
        final Long numberOfApplicationsForUser4 = repository.getNumberOfApplicationOfUser(user4.getId());
        final Long numberOfApplicationsForUserDoesNotExist = repository.getNumberOfApplicationOfUser(5L);

        //then
        assertThat(numberOfApplicationsForUser1).isEqualTo(2);
        assertThat(numberOfApplicationsForUser2).isEqualTo(1);
        assertThat(numberOfApplicationsForUser3).isEqualTo(3);
        assertThat(numberOfApplicationsForUser4).isEqualTo(0);
        assertThat(numberOfApplicationsForUserDoesNotExist).isEqualTo(0);
    }

    @Test
    public void searchApplicationOfUser_returns_the_application_for_the_given_user() throws Exception {
        //given
        SUser user1 = aUser().withId(1L).withUserName("walter.bates").build();
        repository.add(user1);
        SUser user2 = aUser().withId(2L).withUserName("helen.kelly").build();
        repository.add(user2);
        SUser user3 = aUser().withId(3L).withUserName("daniela.angelo").build();
        repository.add(user3);
        SUser user4 = aUser().withId(4L).withUserName("jan.fisher").build();
        repository.add(user4);

        final String profileName1 = "firstProfile";
        final SProfile profile1 = aProfile().withName(profileName1).build();
        repository.add(profile1);
        repository.add(aProfileMember().withUserId(user1.getId()).withProfileId(profile1.getId()).build());
        repository.add(aProfileMember().withUserId(user3.getId()).withProfileId(profile1.getId()).build());

        final String profileName2 = "secondProfile";
        final SProfile profile2 = aProfile().withName(profileName2).build();
        repository.add(profile2);
        repository.add(aProfileMember().withUserId(user2.getId()).withProfileId(profile2.getId()).build());
        repository.add(aProfileMember().withUserId(user3.getId()).withProfileId(profile2.getId()).build());

        final SApplication application1 = repository
                .add(anApplication().withToken("app1").withDisplayName("my app1").withDisplayName("my app1")
                        .withVersion("1.0").withPath("app1").withProfile(profile1.getId()).build());
        final SApplication application2 = repository
                .add(anApplication().withToken("app2").withDisplayName("my app2").withDisplayName("my app2")
                        .withDisplayName("my app2").withVersion("1.0").withPath("/app2").withProfile(profile1.getId())
                        .build());
        final SApplication application3 = repository
                .add(anApplication().withToken("app3").withDisplayName("my app3").withDisplayName("my app3")
                        .withVersion("1.0").withPath("app3").withProfile(profile2.getId()).build());

        repository.flush();

        //when
        final List<SApplication> numberOfApplicationsForUser1 = repository.searchApplicationOfUser(user1.getId());
        final List<SApplication> numberOfApplicationsForUser2 = repository.searchApplicationOfUser(user2.getId());
        final List<SApplication> numberOfApplicationsForUser3 = repository.searchApplicationOfUser(user3.getId());
        final List<SApplication> numberOfApplicationsForUser4 = repository.searchApplicationOfUser(user4.getId());
        final List<SApplication> numberOfApplicationsForUserDoesNotExist = repository.searchApplicationOfUser(5L);

        //then
        assertThat(numberOfApplicationsForUser1).containsExactly(application1, application2);
        assertThat(numberOfApplicationsForUser2).containsExactly(application3);
        assertThat(numberOfApplicationsForUser3).containsExactly(application1, application2, application3);
        assertThat(numberOfApplicationsForUser4).isEmpty();
        assertThat(numberOfApplicationsForUserDoesNotExist).isEmpty();
    }

}
